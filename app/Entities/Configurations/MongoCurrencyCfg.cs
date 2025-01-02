using System.ComponentModel.DataAnnotations;

namespace Entities.Configurations;

public sealed class MongoCurrencyCfg
{
    public const string Section = "MongoCluster";
    [Required] public string BaseUrl { get; set; } = null!;
    [Required] public string UserName { get; set; } = null!;
    [Required] public string Password { get; set; } = null!;
    public string CurrencyDataBaseName { get; set; } = "CurrencyRates";
}

public sealed class MongoLogCfg
{
    public const string Section = "MongoLog";
    [Required] public string BaseUrl { get; set; } = null!;
    [Required] public string UserName { get; set; } = null!;
    [Required] public string Password { get; set; } = null!;
    public string CurrencyDataBaseName { get; set; } = "Logging";
}