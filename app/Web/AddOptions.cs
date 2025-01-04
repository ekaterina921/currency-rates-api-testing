using Entities.Configurations;
using Microsoft.Extensions.Options;

namespace Web;

public static class BuilderExtension
{
    public static void AddOptions(this WebApplicationBuilder builder)
    {
        builder.Services.AddOptions<BankApi>().BindConfiguration(BankApi.Section);
        
        builder.Services.AddOptions<MongoCurrencyCfg>().BindConfiguration(MongoCurrencyCfg.Section);
        builder.Services.AddSingleton<IValidateOptions<MongoCurrencyCfg>, ValidateSettingsOptionsMongoCurrencyCfg>();   
    }
}

[OptionsValidator]
public partial class ValidateSettingsOptionsMongoCurrencyCfg : IValidateOptions<MongoCurrencyCfg>;