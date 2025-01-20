using Entities.Configurations;
using Microsoft.Extensions.Options;
using MongoDB.Driver;

namespace Infrastructure;

public sealed class MongoProvider
{
    private readonly IMongoDatabase _mongoDatabase;

    public MongoProvider(IOptions<MongoCurrencyCfg> options)
    {
        var url = new MongoUrlBuilder(options.Value.BaseUrl)
        {
            Username = options.Value.UserName,
            Password = options.Value.Password,
        }.ToMongoUrl();

        _mongoDatabase = new MongoClient(url).GetDatabase(options.Value.CurrencyDataBaseName);
    }

    internal IMongoCollection<CurrencyRateRecord> GetCurrencyCollection(string code) => 
        _mongoDatabase.GetCollection<CurrencyRateRecord>(code);
}