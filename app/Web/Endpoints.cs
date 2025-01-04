using Application;
using Microsoft.AspNetCore.Http.Features;
using Microsoft.AspNetCore.Mvc;

namespace Web;

public static class Endpoints
{
    public static void AddEndpoints(this WebApplication app)
    {
        app.MapGet("/rates/{currencyCode}/{date}",
                async (string currencyCode, DateOnly date,
                    [FromServices] CurrencyRateExtractor currencyRateExtractor, CancellationToken ct) =>
                {
                    var result = await currencyRateExtractor.GetRate(currencyCode, date, ct);
                    return result.Match(
                        Results.Ok,
                        notFound => Results.NotFound(),
                        notSupported => Results.BadRequest(new ErrorResponse($"{currencyCode} is not supported")),
                        operationFailed => Results.InternalServerError()
                    );
                })
            .AddEndpointFilter(async (context, next) =>
            {
                try
                {
                    return await next(context);
                }
                catch (Exception exception)
                {
                    app.Logger.LogError(exception, "Unhandled exception");
                    var activity = context.HttpContext.Features.Get<IHttpActivityFeature>()?.Activity;
                    return Results.InternalServerError(new InternalErrorResponse(activity?.Id));
                }
            })
            .AddEndpointFilter(async (context, next) =>
            {
                var date = context.GetArgument<DateOnly>(1);
                if (date < DateOnly.FromDateTime(DateTime.UtcNow.Date))
                    return await next(context);

                app.Logger.LogError("Date is not in the past");
                return Results.BadRequest(new ErrorResponse("Date is not in the past."));
            });

        app.MapGet("/supported-currency/",
            () => Results.Ok(CurrencyRateExtractor.SupportedCurrencies()));

        app.MapGet("/supported-currency/{currencyCode}",
            (string currencyCode) => Results.Ok(new SupportedResponse(currencyCode, CurrencyRateExtractor.SupportedCurrencies(currencyCode))));
    }
}

public readonly record struct ErrorResponse(string Error);
public readonly record struct SupportedResponse(string CurrencyCode, bool IsSupported);
public readonly record struct InternalErrorResponse(string? ActivityId);