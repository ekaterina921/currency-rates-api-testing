using Entities;
using OneOf;

namespace Application;

public class CurrencyRateExtractor
{
    private readonly IStorageProvider _storageProvider;
    private readonly IBankProvider _bankProvider;

    public CurrencyRateExtractor(IStorageProvider storageProvider, IBankProvider bankProvider)
    {
        _storageProvider = storageProvider;
        _bankProvider = bankProvider;
    }

    public async Task<OneOf<CurrencyRate[], NotFound, NotSupported, OperationFailed>> GetRate(string currencyCode, DateOnly date, CancellationToken cancellationToken)
    {
        var storedRates = await _storageProvider.GetRates(currencyCode, date, cancellationToken);
        return await storedRates.Match<Task<OneOf<CurrencyRate[], NotFound, NotSupported, OperationFailed>>>(
            rates => Task.FromResult<OneOf<CurrencyRate[], NotFound, NotSupported, OperationFailed>>(rates),
            async notFound =>
            {
                var bankRates = await _bankProvider.GetRates(currencyCode, date, cancellationToken);
                
                return await bankRates.Match(
                    async rates =>
                    {
                        var result = await _storageProvider.StoreRates(currencyCode, rates, date, cancellationToken);
                        return result.Match(
                            success => OneOf<CurrencyRate[], NotFound, NotSupported, OperationFailed>.FromT0(rates),
                            OneOf<CurrencyRate[], NotFound, NotSupported, OperationFailed>.FromT3
                        );
                    },
                    notSupported => Task.FromResult<OneOf<CurrencyRate[], NotFound, NotSupported, OperationFailed>>(notSupported),
                    operationFailed => Task.FromResult<OneOf<CurrencyRate[], NotFound, NotSupported, OperationFailed>>(operationFailed)
                );
            },
            operationFailed => Task.FromResult<OneOf<CurrencyRate[], NotFound, NotSupported, OperationFailed>>(operationFailed)
        );
    }

    public static string[] SupportedCurrencies() => IBankProvider.SupportedCurrencies();
    public static bool SupportedCurrencies(string currencyCode) => IBankProvider.SupportedCurrencies(currencyCode);
}