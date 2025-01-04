using Entities.Configurations;
using MongoDB.Driver;
using Serilog;
using Serilog.Events;
using RollingInterval = Serilog.Sinks.MongoDB.RollingInterval;

namespace Web;

public static class Logging
{
    public static void AddSeriLogConfiguration(this WebApplicationBuilder builder)
    {
        var connectionStr = builder.Configuration.GetSection(MongoLogCfg.Section).Get<MongoLogCfg>()
                            ?? throw new Exception("Cannot get configuration to MongoDbLog");

        builder.Logging.ClearProviders();
        builder.Logging.Configure(options => options.ActivityTrackingOptions = ActivityTrackingOptions.TraceId | ActivityTrackingOptions.SpanId);
        builder.Host.UseSerilog((ctx, cfg) => cfg
            .MinimumLevel.Override("Microsoft.AspNetCore.Hosting", LogEventLevel.Warning)
            .MinimumLevel.Override("Microsoft.AspNetCore.Mvc", LogEventLevel.Warning)
            .MinimumLevel.Override("Microsoft.AspNetCore.Routing", LogEventLevel.Warning)
            .MinimumLevel.Override("Polly", LogEventLevel.Warning)
            .ReadFrom.Configuration(ctx.Configuration)
            .Enrich.FromLogContext()
            .Enrich.WithActivityId()
            .Enrich.WithProperty("application-name", "currency-rate")
            .WriteTo.Console()
            .WriteTo.File("logs/system-log.txt", rollingInterval: Serilog.RollingInterval.Day)
            .WriteTo.MongoDBBson(mongoLogCfg =>
            {
                var url = new MongoUrlBuilder(connectionStr.BaseUrl)
                {
                    Username = connectionStr.UserName,
                    Password = connectionStr.Password,
                }.ToMongoUrl();
                var mongoDbInstance = new MongoClient(url).GetDatabase("Logging");
		
                mongoLogCfg.SetMongoDatabase(mongoDbInstance);
                mongoLogCfg.SetRollingInternal(RollingInterval.Month);
            })
            .Filter.ByExcluding($"RequestPath like '/{Constants.HealthCheckPath}%'"));

        Serilog.Debugging.SelfLog.Enable(Console.Error);
    }

    public static void UseSerilogConfiguration(this WebApplication app)
    {
        app.UseSerilogRequestLogging(options => options.EnrichDiagnosticContext = (ctx, context) =>
        {
            ctx.Set("TraceIdentifier", context.TraceIdentifier);
            ctx.Set("ConnectionId", context.Connection.Id);
        });
    }
}