using Entities;
using OneOf;

namespace Application;

public interface IBankProvider
{
    Task<OneOf<CurrencyRate[], NotSupported, OperationFailed>> GetRates(string currencyCode, DateOnly date, CancellationToken cancellationToken);

    public static string[] SupportedCurrencies() => 
        [Currency.Codes.USD, Currency.Codes.EUR, Currency.Codes.GBP, Currency.Codes.AUD, Currency.Codes.CAD];
    
    public static bool SupportedCurrencies(string currencyCode) => 
        SupportedCurrencies().Any(s => s.Equals(currencyCode, StringComparison.OrdinalIgnoreCase));
}

