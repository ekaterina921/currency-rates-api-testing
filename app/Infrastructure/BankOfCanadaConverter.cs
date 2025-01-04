using System.Globalization;
using System.Text.Json;
using System.Text.Json.Serialization;
using Entities;

namespace Infrastructure;

public sealed class BankOfCanadaConverter : JsonConverter<RateData>
{
    private readonly record struct ReadResult(CurrencyRate[] Rates, string? CurrencyCode);

    private ReadResult ReadRec(ref Utf8JsonReader reader, ReadResult result, in int initDepth)
    {
        if (reader.Read() && reader.CurrentDepth > initDepth)
        {
            if (reader.TokenType is not (JsonTokenType.String or JsonTokenType.PropertyName))
                return ReadRec(ref reader, result, in initDepth);
            
            var str = reader.GetString()!;
            if (str.Equals("d") || str.Equals("v"))
                return ReadRec(ref reader, result, in initDepth);

            return decimal.TryParse(str, NumberStyles.Any, CultureInfo.InvariantCulture, out var d) 
                ? ReadRec(ref reader, new ReadResult([..result.Rates, new CurrencyRate(result.CurrencyCode!, d)], null), in initDepth) 
                : ReadRec(ref reader, result with { CurrencyCode = str }, in initDepth);
        }

        return result;
    }

    public override RateData Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
    {
        var initDepth = reader.CurrentDepth;
        string? currencyCode = null;
        List<CurrencyRate> rates = [];
        while (true)
        {
            if (!reader.Read() || reader.CurrentDepth <= initDepth)
            {
                break;
            }

            if (reader.TokenType is not (JsonTokenType.String or JsonTokenType.PropertyName)) 
                continue;

            var str = reader.GetString()!;
            if (str.Equals("d") || str.Equals("v")) 
                continue;

            if (decimal.TryParse(str, NumberStyles.Any, CultureInfo.InvariantCulture, out var d))
            {
                rates.Add(new CurrencyRate(currencyCode!, d));
                currencyCode = null;
                continue;
            }

            currencyCode = str;
        }

        var data = rates.ToArray();
        return new RateData(data);
    }

    public override void Write(Utf8JsonWriter writer, RateData value, JsonSerializerOptions options)
    {
        throw new NotImplementedException();
    }
}

internal sealed record BankOfCanadaResponse(
    [property: JsonPropertyName("observations")]
    RateData[] Observations);

public sealed record RateData(CurrencyRate[] Value);