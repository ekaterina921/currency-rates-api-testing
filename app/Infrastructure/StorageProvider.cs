using Application;
using Entities;
using MongoDB.Bson.Serialization.Attributes;
using MongoDB.Driver;
using MongoDB.Driver.Linq;
using OneOf;

namespace Infrastructure;

public sealed class StorageProvider : IStorageProvider
{
    private readonly ILogger<StorageProvider> _logger;
    private readonly MongoProvider _mongoProvider;
    private static readonly InsertOneOptions DefaultInsertOneOptions = new();

    public StorageProvider(ILogger<StorageProvider> logger, MongoProvider mongoProvider)
    {
        _mongoProvider = mongoProvider;
        _logger = logger;
    }

    public async Task<OneOf<Success, OperationFailed>> StoreRates(string currencyCode, CurrencyRate[] rates, DateOnly date, CancellationToken cancellationToken)
    {
        try
        {
            await _mongoProvider
                .GetCurrencyCollection(currencyCode.ToLower())
                .InsertOneAsync(new CurrencyRateRecord(date, rates), DefaultInsertOneOptions, cancellationToken);
            return new Success();
        }
        catch (Exception exception)
        {
            _logger.LogError(exception, "");
            return new OperationFailed();
        }
    }

    public async Task<OneOf<CurrencyRate[], NotFound, OperationFailed>> GetRates(string currencyCode, DateOnly date, CancellationToken cancellationToken)
    {
        try
        {
            var data = await _mongoProvider
                .GetCurrencyCollection(currencyCode.ToLower())
                .AsQueryable()
                .FirstOrDefaultAsync(record => record.Date == date, cancellationToken: cancellationToken);

            return data is null ? new NotFound() : data.Rates;
        }
        catch (Exception exception)
        {
            _logger.LogError(exception, "");
            return new OperationFailed();
        }
    }
}

internal sealed record CurrencyRateRecord([property: BsonId] DateOnly Date, CurrencyRate[] Rates);