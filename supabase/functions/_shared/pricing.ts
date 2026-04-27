import { adminClient } from "./auth.ts";

type PricingConfig = {
  interestRate: number;
  processingFeeRate: number;
};

export async function getPricingForTenureDays(tenureDays: number): Promise<PricingConfig> {
  const { data, error } = await adminClient
    .from("loan_pricing_config")
    .select("interest_rate, processing_fee_rate")
    .eq("tenure_days", tenureDays)
    .eq("is_active", true)
    .maybeSingle();

  if (!error && data) {
    return {
      interestRate: Number(data.interest_rate),
      processingFeeRate: Number(data.processing_fee_rate),
    };
  }

  return {
    interestRate: Number((0.20 * (tenureDays / 60)).toFixed(6)),
    processingFeeRate: 0.03,
  };
}
