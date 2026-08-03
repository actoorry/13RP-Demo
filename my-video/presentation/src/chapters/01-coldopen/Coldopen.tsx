import { MaskReveal } from "../../components/MaskReveal";
import type { ChapterStepProps } from "../../registry/types";
import "./Coldopen.css";

/**
 * 第 1 章 · coldopen — 停电危机
 *
 * 4 steps: 凌晨两点 → 墨西哥工厂停电 → 冲击数字 → 全乱了。
 * 视觉演示：CSS 模拟时钟 / SVG 工厂灯光逐个熄灭 / hero 数字冲击 / 警报脉冲。
 * 颜色字体全走 token（neon-cyber 主题），动画时长 ≤ 口播时长。
 */
export default function ColdopenChapter({ step }: ChapterStepProps) {
  /* step 0 — 凌晨两点。时钟 + 夜色氛围 */
  if (step === 0) {
    return (
      <div className="cd-scene scene-pad">
        <div className="cd-corner label-mono">
          <span className="dot-accent" />
          &nbsp;02:00 · MEX 工厂区
        </div>

        <div className="cd-cover">
          <div className="cd-kicker kicker">应急演练 · 突发停电</div>
          <h1 className="cd-hero">
            <MaskReveal show duration={650}>
              <span className="serif-cn">凌晨两点。</span>
            </MaskReveal>
          </h1>
          <div className="cd-clock" aria-hidden="true">
            <div className="cd-clock-dial">
              <span className="cd-tick t12" />
              <span className="cd-tick t3" />
              <span className="cd-tick t6" />
              <span className="cd-tick t9" />
              <span className="cd-hand cd-hand-h" />
              <span className="cd-hand cd-hand-m" />
            </div>
            <div className="cd-clock-label mono">MEXICO · LOCAL TIME</div>
          </div>
        </div>

        <div className="cd-foot label-mono">
          <span className="cd-arrow">→</span> 点击推进
        </div>
      </div>
    );
  }

  /* step 1 — 墨西哥工厂，停电了。SVG 工厂熄灯 + ALERT */
  if (step === 1) {
    return (
      <div className="cd-scene scene-pad">
        <div className="cd-corner label-mono">
          <span className="dot-accent" />
          &nbsp;MEXICO · 北美核心供应节点
        </div>

        <div className="cd-cover cd-cover-factory">
          <h1 className="cd-hero cd-hero-sm">
            <MaskReveal show duration={700}>
              <span className="serif-cn">墨西哥工厂，</span>
            </MaskReveal>
            <MaskReveal show delay={350} duration={700}>
              <span className="serif-cn cd-em">停电了。</span>
            </MaskReveal>
          </h1>

          {/* 工厂剪影：三栋厂房灯光逐个熄灭 */}
          <svg className="cd-factory" viewBox="0 0 760 320" fill="none" aria-hidden="true">
            {/* ground */}
            <line x1="0" y1="300" x2="760" y2="300" stroke="var(--rule)" strokeWidth="1" />
            {/* plant 1 */}
            <g className="cd-plant">
              <rect x="40" y="160" width="180" height="140" fill="var(--surface-2)" stroke="var(--rule)" />
              <rect x="100" y="110" width="60" height="50" fill="var(--surface-2)" stroke="var(--rule)" />
              <rect x="115" y="60" width="30" height="50" fill="var(--surface-3)" stroke="var(--rule)" />
              <rect className="cd-window cd-window-a" x="60" y="185" width="24" height="20" fill="var(--accent)" opacity="0.9" />
              <rect className="cd-window cd-window-b" x="100" y="185" width="24" height="20" fill="var(--accent)" opacity="0.9" />
              <rect className="cd-window cd-window-c" x="140" y="185" width="24" height="20" fill="var(--accent)" opacity="0.9" />
              <rect className="cd-window cd-window-d" x="180" y="185" width="24" height="20" fill="var(--accent)" opacity="0.9" />
            </g>
            {/* plant 2 */}
            <g className="cd-plant">
              <rect x="290" y="200" width="180" height="100" fill="var(--surface-2)" stroke="var(--rule)" />
              <rect x="340" y="150" width="80" height="50" fill="var(--surface-2)" stroke="var(--rule)" />
              <rect className="cd-window cd-window-e" x="310" y="220" width="24" height="20" fill="var(--accent)" opacity="0.9" />
              <rect className="cd-window cd-window-f" x="350" y="220" width="24" height="20" fill="var(--accent)" opacity="0.9" />
              <rect className="cd-window cd-window-g" x="390" y="220" width="24" height="20" fill="var(--accent)" opacity="0.9" />
              <rect className="cd-window cd-window-h" x="430" y="220" width="24" height="20" fill="var(--accent)" opacity="0.9" />
            </g>
            {/* plant 3 */}
            <g className="cd-plant">
              <rect x="540" y="180" width="180" height="120" fill="var(--surface-2)" stroke="var(--rule)" />
              <rect x="600" y="130" width="60" height="50" fill="var(--surface-2)" stroke="var(--rule)" />
              <rect className="cd-window cd-window-i" x="560" y="205" width="24" height="20" fill="var(--accent)" opacity="0.9" />
              <rect className="cd-window cd-window-j" x="600" y="205" width="24" height="20" fill="var(--accent)" opacity="0.9" />
              <rect className="cd-window cd-window-k" x="640" y="205" width="24" height="20" fill="var(--accent)" opacity="0.9" />
              <rect className="cd-window cd-window-l" x="680" y="205" width="24" height="20" fill="var(--accent)" opacity="0.9" />
            </g>
            {/* power lines */}
            <path d="M40 160 L40 40 M40 40 L760 40 M760 40 L760 180" stroke="var(--text-faint)" strokeWidth="1" strokeDasharray="6 6" />
          </svg>

          <div className="cd-alert label-mono">
            <span className="cd-alert-dot" /> ALERT · POWER OUTAGE DETECTED
          </div>
        </div>

        <div className="cd-foot label-mono">
          <span className="cd-arrow">→</span> 点击推进
        </div>
      </div>
    );
  }

  /* step 2 — 冲击数字：5 天 / 3000 台 / 下周交付 */
  if (step === 2) {
    return (
      <div className="cd-scene scene-pad">
        <div className="cd-corner label-mono">
          <span className="dot-accent" />
          &nbsp;IMPACT · 影响半径
        </div>

        <div className="cd-nums">
          <div className="cd-num-item cd-num-1">
            <div className="cd-num-value hero-num">5</div>
            <div className="cd-num-unit label-mono">天 · 停电时长</div>
          </div>
          <div className="cd-num-item cd-num-2">
            <div className="cd-num-value hero-num">3000</div>
            <div className="cd-num-unit label-mono">台 · 电驱系统</div>
          </div>
          <div className="cd-num-item cd-num-3">
            <div className="cd-num-value hero-num">下周</div>
            <div className="cd-num-unit label-mono">交付节点</div>
          </div>
        </div>

        <div className="cd-nums-caption">
          <MaskReveal show delay={500} duration={650}>
            <span className="serif-cn cd-nums-line">三千台电驱系统，下周就要交付。</span>
          </MaskReveal>
        </div>

        <div className="cd-foot label-mono">
          <span className="cd-arrow">→</span> 点击推进
        </div>
      </div>
    );
  }

  /* step 3 — 这一下，全乱了。警报脉冲收束 */
  if (step === 3) {
    return (
      <div className="cd-scene scene-pad cd-close">
        <div className="cd-close-ring" aria-hidden="true" />
        <div className="cd-close-inner">
          <div className="cd-kicker kicker">连锁反应 · 启动</div>
          <div className="cd-close-quote">
            <MaskReveal show duration={750}>
              <span className="serif-cn">这一下，</span>
            </MaskReveal>
            <MaskReveal show delay={300} duration={750}>
              <span className="serif-cn cd-em">全乱了。</span>
            </MaskReveal>
          </div>
          <div className="cd-close-sub label-mono">整条北美供应链，都在等一个答案</div>
        </div>
        <div className="cd-foot label-mono">
          <span className="cd-arrow">→</span> 点击推进
        </div>
      </div>
    );
  }

  /* 防御兜底：超界 step 不再渲染（narrations 已保证 0..3） */
  return null;
}
