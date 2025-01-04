using Entities;
using OneOf;

namespace Application;

public interface IStorageProvider
{
    Task<OneOf<Success, OperationFailed>> StoreRates(string currencyCode, CurrencyRate[] rates, DateOnly date, CancellationToken cancellationToken);

    Task<OneOf<CurrencyRate[], NotFound, OperationFailed>> GetRates(string currencyCode, DateOnly date, CancellationToken cancellationToken);
}