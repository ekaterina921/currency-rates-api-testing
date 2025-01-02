using Application;

namespace Infrastructure;

public static class RegisterServices
{
    public static void AddInfrastructure(this WebApplicationBuilder builder)
    {
        builder.Services.AddSingleton<MongoProvider>();
        builder.Services.AddTransient<IStorageProvider, StorageProvider>();
        builder.Services.AddHttpClient<IBankProvider, BankProvider>().AddStandardResilienceHandler();
    }
}