<script setup lang="ts">
definePageMeta({ middleware: ["auth"] });

const capabilities = [
  {
    icon: "bi-ui-checks-grid",
    title: "表單設計",
    description: "集中管理表單定義與版本，讓業務資料結構清楚且容易維護。",
  },
  {
    icon: "bi-diagram-3",
    title: "流程編排",
    description: "以視覺化方式設計簽核流程，串接組織、角色與任務規則。",
  },
  {
    icon: "bi-shield-check",
    title: "權限治理",
    description: "以 Tenant 為邊界管理人員、資料與操作權限，兼顧安全與彈性。",
  },
];
</script>

<template>
  <main class="about-page">
    <section class="hero-card">
      <div class="hero-glow hero-glow-one"></div>
      <div class="hero-glow hero-glow-two"></div>

      <div class="hero-content">
        <img class="brand-logo" src="/img/flowmint-logo.svg" alt="FlowMint">
        <div>
          <span class="eyebrow">WORKFLOW PLATFORM</span>
          <h1>讓每一段流程，<br />都清楚地向前流動。</h1>
          <p>
            FlowMint 整合表單、流程、組織與簽核管理，協助團隊用一致的方式建立、發布並執行企業工作流程。
          </p>
          <div class="brand-tags" aria-label="FlowMint 核心特色">
            <span><i class="bi bi-check-circle-fill"></i> 可配置</span>
            <span><i class="bi bi-check-circle-fill"></i> 可追蹤</span>
            <span><i class="bi bi-check-circle-fill"></i> 可治理</span>
          </div>
        </div>
      </div>
    </section>

    <section class="capability-section">
      <div class="section-heading">
        <div>
          <span class="eyebrow text-primary">WHAT FLOWMINT DOES</span>
          <h2>從設計到執行，集中在同一個平台</h2>
        </div>
        <p>讓流程規則、表單版本與組織權責彼此連結，降低分散維護造成的落差。</p>
      </div>

      <div class="capability-grid">
        <article
          v-for="(item, index) in capabilities"
          :key="item.title"
          class="capability-card"
        >
          <div class="capability-number">0{{ index + 1 }}</div>
          <div class="capability-icon">
            <i class="bi" :class="item.icon"></i>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
        </article>
      </div>
    </section>

    <section class="vision-card">
      <div class="vision-icon"><i class="bi bi-stars"></i></div>
      <div>
        <span class="eyebrow text-primary">OUR VISION</span>
        <h2>把複雜留給平台，把專注留給團隊。</h2>
        <p class="mb-0">
          FlowMint 希望讓企業流程不再只是散落的規則，而是透明、可靠且能持續演進的數位資產。
        </p>
      </div>
    </section>
  </main>
</template>

<style scoped>
.about-page {
  --fm-blue: #2563eb;
  --fm-mint: #20c997;
  --fm-ink: #172033;
  --fm-muted: #64748b;
  padding: 0.5rem 0 2.5rem;
  color: var(--fm-ink);
}

.hero-card {
  position: relative;
  min-height: 25rem;
  padding: clamp(2.25rem, 6vw, 5rem);
  overflow: hidden;
  border-radius: 1.5rem;
  color: #fff;
  background:
    linear-gradient(120deg, rgba(13, 34, 73, 0.98), rgba(24, 76, 137, 0.93)),
    radial-gradient(circle at top right, var(--fm-mint), transparent 45%);
  box-shadow: 0 1.5rem 3.5rem rgba(30, 64, 175, 0.2);
}

.hero-card::after {
  position: absolute;
  right: -3rem;
  bottom: -7rem;
  width: 24rem;
  height: 24rem;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 50%;
  content: "";
}

.hero-content {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: auto minmax(0, 48rem);
  align-items: start;
  gap: clamp(1.5rem, 4vw, 3rem);
}

.brand-logo {
  display: block;
  width: clamp(10rem, 18vw, 14rem);
  height: auto;
}

.eyebrow {
  display: inline-block;
  margin-bottom: 0.8rem;
  color: #8fe8d0;
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.17em;
}

.hero-card h1 {
  max-width: 46rem;
  margin-bottom: 1.25rem;
  font-size: clamp(2.25rem, 5vw, 4.25rem);
  font-weight: 800;
  line-height: 1.12;
  letter-spacing: -0.045em;
}

.hero-card p {
  max-width: 42rem;
  margin-bottom: 1.75rem;
  color: rgba(255, 255, 255, 0.78);
  font-size: 1.05rem;
  line-height: 1.85;
}

.brand-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.7rem;
}

.brand-tags span {
  padding: 0.55rem 0.9rem;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  font-size: 0.88rem;
}

.brand-tags i {
  margin-right: 0.35rem;
  color: #72dfc2;
}

.hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(4px);
}

.hero-glow-one {
  top: -8rem;
  right: 5%;
  width: 20rem;
  height: 20rem;
  background: rgba(32, 201, 151, 0.18);
}

.hero-glow-two {
  bottom: -9rem;
  left: 18%;
  width: 18rem;
  height: 18rem;
  background: rgba(59, 130, 246, 0.2);
}

.capability-section {
  padding: clamp(3rem, 6vw, 5rem) 0;
}

.section-heading {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(18rem, 0.85fr);
  align-items: end;
  gap: 2rem;
  margin-bottom: 2rem;
}

.section-heading h2,
.vision-card h2 {
  margin: 0;
  font-size: clamp(1.65rem, 3vw, 2.4rem);
  font-weight: 750;
  line-height: 1.25;
  letter-spacing: -0.025em;
}

.section-heading p,
.vision-card p,
.capability-card p {
  color: var(--fm-muted);
  line-height: 1.75;
}

.capability-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1rem;
}

.capability-card {
  position: relative;
  min-height: 15rem;
  padding: 1.75rem;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  border-radius: 1.15rem;
  background: #fff;
  box-shadow: 0 0.75rem 2rem rgba(15, 23, 42, 0.055);
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.capability-card:hover {
  transform: translateY(-4px);
  border-color: rgba(37, 99, 235, 0.35);
  box-shadow: 0 1.25rem 2.5rem rgba(37, 99, 235, 0.11);
}

.capability-number {
  position: absolute;
  top: 1.2rem;
  right: 1.4rem;
  color: #e2e8f0;
  font-size: 2.4rem;
  font-weight: 800;
}

.capability-icon,
.vision-icon {
  display: grid;
  width: 3rem;
  height: 3rem;
  margin-bottom: 1.4rem;
  place-items: center;
  border-radius: 0.85rem;
  color: var(--fm-blue);
  background: linear-gradient(135deg, #eaf2ff, #e7fff8);
  font-size: 1.35rem;
}

.capability-card h3 {
  margin-bottom: 0.7rem;
  font-size: 1.15rem;
  font-weight: 750;
}

.capability-card p {
  margin: 0;
  font-size: 0.94rem;
}

.vision-card {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 1.4rem;
  align-items: start;
  padding: clamp(1.75rem, 4vw, 3rem);
  border: 1px solid #dbeafe;
  border-radius: 1.25rem;
  background: linear-gradient(135deg, #f8fbff, #f0fdfa);
}

.vision-icon {
  width: 3.5rem;
  height: 3.5rem;
  margin: 0;
  color: #fff;
  background: linear-gradient(135deg, var(--fm-blue), var(--fm-mint));
}

.vision-card h2 {
  margin-bottom: 0.75rem;
}

@media (max-width: 767.98px) {
  .hero-content,
  .section-heading,
  .capability-grid {
    grid-template-columns: 1fr;
  }

  .hero-card {
    min-height: auto;
    border-radius: 1.1rem;
  }

  .brand-logo {
    width: 11rem;
  }

  .section-heading {
    gap: 0.75rem;
  }

  .vision-card {
    grid-template-columns: 1fr;
  }
}
</style>
