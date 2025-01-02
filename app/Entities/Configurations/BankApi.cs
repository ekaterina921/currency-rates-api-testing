namespace Entities.Configurations;

public class BankApi
{
    public const string Section = "BankApi";
    
    public string EcbStat { get; set; } = "https://data-api.ecb.europa.eu/service/data";

    public string BankOfCanada { get; set; } = "https://www.bankofcanada.ca/valet/observations/group/FX_RATES_DAILY/json";
    
    public string ReserveBankOfAustralia { get; set; } = "https://www.rba.gov.au/statistics/tables/csv/f11.1-data.csv";
}