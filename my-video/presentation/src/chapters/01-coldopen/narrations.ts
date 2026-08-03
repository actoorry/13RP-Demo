import type { Narration } from "../../registry/types";

/**
 * coldopen — 停电危机
 *
 * Length === number of steps rendered in Coldopen.tsx (4).
 * Index i === spoken text for `step === i`.
 *
 * Audio synthesis reads this file directly (scripts/extract-narrations.ts).
 * Auto mode plays public/audio/coldopen/<i+1>.mp3 and advances on audio end.
 */
export const narrations: Narration[] = [
  // step 0 — 凌晨两点
  "凌晨两点。",
  // step 1 — 墨西哥工厂停电
  "墨西哥工厂，停电了。通知说，至少五天。",
  // step 2 — 冲击数字
  "三千台电驱系统，下周就要交付。",
  // step 3 — 全乱了
  "这一下，全乱了。",
];
