using System.Text.Json.Serialization;
using Application;
using Entities;
using Entities.Configurations;
using Infrastructure;
using Web;

var builder = WebApplication.CreateSlimBuilder(args);
builder.AddSeriLogConfiguration();
builder.AddOptions();
builder.AddApplication();
builder.AddInfrastructure();

builder.Services.ConfigureHttpJsonOptions(options =>
{
    options.SerializerOptions.TypeInfoResolverChain.Insert(0, AppJsonSerializerContext.Default);
});

var app = builder.Build();
app.UseSerilogConfiguration();
app.AddEndpoints();

app.Run();

[JsonSourceGenerationOptions(DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull)]
[JsonSerializable(typeof(CurrencyRate))]
internal partial class AppJsonSerializerContext : JsonSerializerContext;