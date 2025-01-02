using System.Globalization;
using System.Text.Json.Serialization;
using System.Xml;
using Application;
using Entities;
using Entities.Configurations;
using Microsoft.Extensions.Options;
using OneOf;

namespace Infrastructure;

public sealed class BankProvider : IBankProvider
{
    private readonly ILogger<BankProvider> _logger;
    private readonly HttpClient _httpClient;
    private readonly IOptionsSnapshot<BankApi> _bankApi;

    public BankProvider(HttpClient httpClient, ILogger<BankProvider> logger, IOptionsSnapshot<BankApi> bankApi)
    {
        _httpClient = httpClient;
        _logger = logger;
        _bankApi = bankApi;
    }

    public async Task<OneOf<CurrencyRate[], NotSupported, OperationFailed>>
        GetRates(string currencyCode, DateOnly date, CancellationToken cancellationToken)
    {
        using var _ = _logger.BeginScope("Bank request");

        switch (currencyCode)
        {
            case not null when currencyCode.Equals(Currency.Codes.EUR, StringComparison.OrdinalIgnoreCase):
                var ecbRates = await EcbRates(date, cancellationToken);
                return ecbRates.Match(
                    OneOf<CurrencyRate[], NotSupported, OperationFailed>.FromT0,
                    OneOf<CurrencyRate[], NotSupported, OperationFailed>.FromT2
                );

            case not null when currencyCode.Equals(Currency.Codes.USD, StringComparison.OrdinalIgnoreCase):
                var usdRates = await UsdRates(date, cancellationToken);
                return usdRates.Match(
                    OneOf<CurrencyRate[], NotSupported, OperationFailed>.FromT0,
                    OneOf<CurrencyRate[], NotSupported, OperationFailed>.FromT2
                );

            case not null when currencyCode.Equals(Currency.Codes.GBP, StringComparison.OrdinalIgnoreCase):
                var gbpRates = await GbpRates(date, cancellationToken);
                return gbpRates.Match(
                    OneOf<CurrencyRate[], NotSupported, OperationFailed>.FromT0,
                    OneOf<CurrencyRate[], NotSupported, OperationFailed>.FromT2
                );

            case not null when currencyCode.Equals(Currency.Codes.CAD, StringComparison.OrdinalIgnoreCase):
                var cadRates = await CadRates(date, cancellationToken);
                return cadRates.Match(
                    OneOf<CurrencyRate[], NotSupported, OperationFailed>.FromT0,
                    OneOf<CurrencyRate[], NotSupported, OperationFailed>.FromT2
                );

            case not null when currencyCode.Equals(Currency.Codes.AUD, StringComparison.OrdinalIgnoreCase):
                var audRates = await AudRates(date, cancellationToken);
                return audRates.Match(
                    OneOf<CurrencyRate[], NotSupported, OperationFailed>.FromT0,
                    OneOf<CurrencyRate[], NotSupported, OperationFailed>.FromT2
                );

            default:
                return OneOf<CurrencyRate[], NotSupported, OperationFailed>.FromT1(new NotSupported());
        }
    }

    private async Task<OneOf<CurrencyRate[], OperationFailed>> EcbRates(DateOnly date, CancellationToken cancellationToken)
    {
        using var scope = _logger.BeginScope(nameof(EcbRates));

        List<CurrencyRate> result = [];
        string[] codes =
        [
            Currency.Codes.JPY,
            Currency.Codes.BGN,
            Currency.Codes.CZK,
            Currency.Codes.DKK,
            Currency.Codes.GBP,
            Currency.Codes.HUF,
            Currency.Codes.PLN,
            Currency.Codes.RON,
            Currency.Codes.SEK,
            Currency.Codes.CHF,
            Currency.Codes.ISK,
            Currency.Codes.NOK,
            Currency.Codes.TRY,
            Currency.Codes.AUD,
            Currency.Codes.BRL,
            Currency.Codes.CAD,
            Currency.Codes.CNY,
            Currency.Codes.HKD,
            Currency.Codes.IDR,
            Currency.Codes.ILS,
            Currency.Codes.INR,
            Currency.Codes.KRW,
            Currency.Codes.MXN,
            Currency.Codes.MYR,
            Currency.Codes.NZD,
            Currency.Codes.PHP,
            Currency.Codes.SGD,
            Currency.Codes.THB,
            Currency.Codes.ZAR
        ];

        foreach (var code in codes)
        {
            var rate = await GetLatestRates(code);
            if (rate.IsT0)
                result.Add(rate.AsT0);
            else
                return new OperationFailed();
        }

        return result.ToArray();

        async Task<OneOf<CurrencyRate, OperationFailed>> GetLatestRates(string currencyCode)
        {
            var executionAttempt = 0;
            while (executionAttempt < 14)
            {
                var xml = await _httpClient.GetStringAsync(
                    $"{_bankApi.Value.EcbStat}/EXR/D.{currencyCode}.EUR.SP00.A?startPeriod={date.ToString("O")}&endPeriod={date.ToString("O")}",
                    cancellationToken);

                if (string.IsNullOrEmpty(xml))
                {
                    date = date.AddDays(-1);
                    executionAttempt++;
                    continue;
                }

                var xmlDoc = new XmlDocument();
                xmlDoc.LoadXml(xml);
                var nsmgr = new XmlNamespaceManager(xmlDoc.NameTable);
                nsmgr.AddNamespace("message", "http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message");
                nsmgr.AddNamespace("generic", "http://www.sdmx.org/resources/sdmxml/schemas/v2_1/data/generic");

                var rate = xmlDoc
                    .SelectSingleNode("//generic:ObsValue", nsmgr)
                    ?.Attributes
                    ?.GetNamedItem("value")
                    ?.Value ?? throw new Exception("Rate from ECB XML is null");

                return new CurrencyRate(currencyCode,
                    decimal.Parse(rate, NumberStyles.Any, CultureInfo.InvariantCulture));
            }

            _logger.LogError("Cannot get rate for {CurrencyCode} from ECB", currencyCode);
            return new OperationFailed();
        }
    }

    private async Task<OneOf<CurrencyRate[], OperationFailed>> CadRates(DateOnly date, CancellationToken cancellationToken)
    {
        using var scope = _logger.BeginScope(nameof(CadRates));

        var executionAttempt = 0;
        while (executionAttempt <= 14)
        {
            var requestUri = $"{_bankApi.Value.BankOfCanada}?start_date={date.ToString("O")}&end_date={date.ToString("O")}";
            var response = await _httpClient.GetFromJsonAsync<BankOfCanadaResponse>(requestUri, InfrastructureJsonSerializerContext.Default.BankOfCanadaResponse, cancellationToken);

            if (response?.Observations.FirstOrDefault()?.Value is not null)
                return response.Observations
                    .FirstOrDefault()!
                    .Value
                    .Select(r => new CurrencyRate(CurrencyCode: r.CurrencyCode.Substring(2, 3), Rate: 1 / r.Rate))
                    .ToArray();

            date = date.AddDays(-1);
            executionAttempt++;
        }

        _logger.LogError("Rates from Bank of Canada cannot be obtained for {RateDate}", date.ToString("O"));
        return new OperationFailed();
    }

    private async Task<OneOf<CurrencyRate[], OperationFailed>> UsdRates(DateOnly date, CancellationToken cancellationToken)
    {
        throw new NotImplementedException();
    }

    private async Task<OneOf<CurrencyRate[], OperationFailed>> GbpRates(DateOnly date, CancellationToken cancellationToken)
    {
        throw new NotImplementedException();
    }

    private async Task<OneOf<CurrencyRate[], OperationFailed>> AudRates(DateOnly date, CancellationToken cancellationToken)
    {
        using var scope = _logger.BeginScope(nameof(AudRates));

        var csv = await _httpClient.GetStringAsync($"{_bankApi.Value.ReserveBankOfAustralia}?v={date.ToString("O")}",
            cancellationToken);

        var rows = csv.Split(["\r\n", "\n", "\n"], StringSplitOptions.RemoveEmptyEntries);

        return GetLatestRates().MapT0(data =>
        {
            var rates = data
                .Split(',')
                .Select((rateStr, index) =>
                {
                    var rate = decimal.TryParse(rateStr, NumberStyles.Any, CultureInfo.InvariantCulture, out var r)
                        ? r
                        : -1;
                    return (Rate: rate, Index: index);
                })
                .Where(rates => rates.Rate > 0)
                .ToArray();

            var codes = rows
                .First(s => s.StartsWith("Title"))
                .Split(',')
                .Select((row, i) => (Code: row.StartsWith("A$1=") ? row.Substring(4, 3) : string.Empty, Index: i))
                .Where(codes => codes.Code is not "")
                .ToArray();

            return codes
                .Join(rates, code => code.Index, rate => rate.Index,
                    (code, rate) => new CurrencyRate(code.Code, rate.Rate))
                .ToArray();
        });

        OneOf<string, OperationFailed> GetLatestRates()
        {
            var executionAttempt = 0;
            while (executionAttempt <= 14)
            {
                var rateString = rows.LastOrDefault(s => s.StartsWith(date.ToString("dd-MMM-yyyy")));
                if (rateString is not null)
                    return rateString;

                date = date.AddDays(-1);
                executionAttempt++;
            }

            _logger.LogError("Rates from Bank of Australia cannot be obtained for {RateDate}", date.ToString("O"));
            return new OperationFailed();
        }
    }
}

[JsonSourceGenerationOptions(DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull, Converters = [ typeof(BankOfCanadaConverter) ])]
[JsonSerializable(typeof(BankOfCanadaResponse))]
internal partial class InfrastructureJsonSerializerContext : JsonSerializerContext;