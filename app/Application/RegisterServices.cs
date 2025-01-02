namespace Application;

public static class RegisterServices
{
    public static void AddApplication(this WebApplicationBuilder builder)
    {
        builder.Services.AddTransient<CurrencyRateExtractor>();
    }
}