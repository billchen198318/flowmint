<script setup lang="ts">
import { ref } from 'vue';
import { useBaseStore } from '../store/baseStore';
import { toast } from 'vue3-toastify';
import 'vue3-toastify/dist/index.css';
import { setRefreshAndAccessTokenCookie, userLogoutClearCookie } from '../components/BaseHelper';
import { useSwalLoading } from '@/composables/useSwalLoading';

definePageMeta({ layout: 'blank' });

const baseStore = useBaseStore();
const userId = ref('');
const passwd = ref('');
const message = ref('');
const showPassword = ref(false);
const submitting = ref(false);
const { showLoading, hideLoading } = useSwalLoading();

const loginBtnClick = async () => {
  if (!userId.value.trim() || !passwd.value.trim()) {
    toast.warn('請輸入帳戶與密碼!');
    return;
  }

  showLoading();
  submitting.value = true;

  message.value = '';

  try {
    const responseJson: any = await useApi('/auth/signin', {
      method: "POST",
      body: {
        username: userId.value,
        password: passwd.value
      }
    });

    if (responseJson) {
      baseStore.setUserData(responseJson);
      setRefreshAndAccessTokenCookie(responseJson.refreshToken, responseJson.accessToken);
      navigateTo('/workspace');
    } else {
      baseStore.clearUserData();
      userLogoutClearCookie();
    }
  } catch (error: any) {
    userLogoutClearCookie();
    console.log(error);
    message.value = error.response?._data?.message || 'Login failed';
    userId.value = '';
    passwd.value = '';
  } finally {
    submitting.value = false;
    hideLoading();
  }
};
</script>

<template>
  <main class="login-page">
    <div class="ambient ambient-one"></div>
    <div class="ambient ambient-two"></div>
    <section class="login-shell">
      <aside class="brand-panel">
        <img class="brand-logo" src="/img/flowmint-logo.svg" alt="FlowMint">
        <div class="brand-copy">
          <span class="eyebrow">Enterprise Workflow</span>
          <h1>讓每一次簽核，<br><span>清楚而流暢。</span></h1>
          <p>FlowMint 將表單、流程、組織與稽核軌跡整合在同一個工作空間。</p>
        </div>
        <div class="feature-list">
          <div><i class="bi bi-diagram-3"></i><span>彈性流程設計</span></div>
          <div><i class="bi bi-shield-check"></i><span>完整權限與稽核</span></div>
          <div><i class="bi bi-graph-up-arrow"></i><span>即時營運洞察</span></div>
        </div>
        <small>FLOWMINT · WORKFLOW, MADE CLEAR</small>
      </aside>

      <section class="auth-panel">
        <div class="mobile-brand">
          <img src="/img/flowmint-logo.svg" alt="FlowMint">
        </div>
        <form class="login-card" @submit.prevent="loginBtnClick">
          <header>
            <span class="eyebrow">WELCOME BACK</span>
            <h2>登入 FlowMint</h2>
            <p>請使用您的企業帳戶繼續。</p>
          </header>

          <div class="field-group">
            <label for="username">帳戶</label>
            <div class="input-wrap">
              <i class="bi bi-person"></i>
              <input id="username" v-model="userId" type="text" autocomplete="username" placeholder="輸入您的帳戶" autofocus>
            </div>
          </div>

          <div class="field-group">
            <label for="password">密碼</label>
            <div class="input-wrap">
              <i class="bi bi-lock"></i>
              <input id="password" v-model="passwd" :type="showPassword ? 'text' : 'password'" autocomplete="current-password" placeholder="輸入您的密碼">
              <button type="button" class="password-toggle" :aria-label="showPassword ? '隱藏密碼' : '顯示密碼'" @click="showPassword = !showPassword"><i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i></button>
            </div>
          </div>

          <div v-if="message" class="login-alert" role="alert"><i class="bi bi-exclamation-circle"></i><span>{{ message }}</span></div>

          <button type="submit" class="login-button" :disabled="submitting">
            <span v-if="submitting" class="spinner-border spinner-border-sm" aria-hidden="true"></span>
            <span>{{ submitting ? '登入中…' : '登入' }}</span>
            <i v-if="!submitting" class="bi bi-arrow-right"></i>
          </button>
          <p class="security-note"><i class="bi bi-shield-lock"></i> 連線與登入資訊皆受安全機制保護</p>
        </form>
      </section>
    </section>
  </main>
</template>

<style scoped>
.login-page { --ink: #102a43; --muted: #627d98; --mint: #20b486; --mint-dark: #087f6a; min-height: 100vh; display: grid; place-items: center; position: relative; overflow: hidden; padding: 2rem; background: linear-gradient(145deg, #eefaf6 0%, #f6f9fc 48%, #e8f1f8 100%); color: var(--ink); }
.ambient { position: absolute; border-radius: 999px; filter: blur(2px); opacity: .58; pointer-events: none; }
.ambient-one { width: 30rem; height: 30rem; top: -13rem; right: -8rem; background: radial-gradient(circle, rgba(32,180,134,.26), transparent 68%); }
.ambient-two { width: 26rem; height: 26rem; bottom: -14rem; left: -8rem; background: radial-gradient(circle, rgba(57,119,184,.2), transparent 68%); }
.login-shell { width: min(1080px, 100%); min-height: 650px; display: grid; grid-template-columns: 1.08fr .92fr; position: relative; z-index: 1; overflow: hidden; border: 1px solid rgba(255,255,255,.86); border-radius: 28px; background: rgba(255,255,255,.78); box-shadow: 0 30px 80px rgba(16,42,67,.16); backdrop-filter: blur(18px); }
.brand-panel { display: flex; flex-direction: column; padding: 3.25rem; color: white; background: linear-gradient(145deg, #0a665c 0%, #0d826f 48%, #154f70 100%); position: relative; isolation: isolate; }
.brand-panel::after { content: ""; position: absolute; inset: 0; z-index: -1; opacity: .18; background-image: radial-gradient(circle at 1px 1px, white 1px, transparent 0); background-size: 24px 24px; mask-image: linear-gradient(to bottom right, #000, transparent 75%); }
.brand-logo { display: block; width: min(15rem, 72%); height: auto; }
.brand-copy { margin: auto 0 2.6rem; max-width: 29rem; }
.eyebrow { display: block; margin-bottom: 1rem; font-size: .72rem; font-weight: 800; letter-spacing: .18em; color: #9ff5d6; }
.brand-copy h1 { margin: 0 0 1.4rem; font-size: clamp(2.6rem, 5vw, 4.2rem); line-height: 1.08; letter-spacing: -.05em; font-weight: 750; }
.brand-copy h1 span { color: #88f0cf; }
.brand-copy p { max-width: 26rem; margin: 0; color: rgba(255,255,255,.76); font-size: 1.04rem; line-height: 1.8; }
.feature-list { display: flex; flex-wrap: wrap; gap: .7rem; margin-bottom: 2.3rem; }
.feature-list div { display: flex; align-items: center; gap: .55rem; padding: .65rem .85rem; border: 1px solid rgba(255,255,255,.15); border-radius: 12px; background: rgba(255,255,255,.08); font-size: .86rem; }
.brand-panel small { color: rgba(255,255,255,.46); font-size: .67rem; letter-spacing: .16em; }
.auth-panel { display: grid; place-items: center; padding: 3.5rem; background: rgba(255,255,255,.62); }
.login-card { width: min(100%, 380px); }
.login-card header { margin-bottom: 2.3rem; }
.login-card header .eyebrow { color: var(--mint-dark); margin-bottom: .65rem; }
.login-card h2 { margin: 0 0 .7rem; font-size: 2rem; letter-spacing: -.035em; font-weight: 750; }
.login-card header p { margin: 0; color: var(--muted); }
.field-group { margin-bottom: 1.25rem; }
.field-group label { display: block; margin-bottom: .5rem; font-size: .86rem; font-weight: 700; color: #334e68; }
.input-wrap { display: flex; align-items: center; min-height: 54px; border: 1px solid #d7e3ec; border-radius: 13px; background: #fff; transition: border-color .2s, box-shadow .2s, transform .2s; }
.input-wrap:focus-within { border-color: var(--mint); box-shadow: 0 0 0 4px rgba(32,180,134,.12); transform: translateY(-1px); }
.input-wrap > i { margin-left: 1rem; color: #829ab1; }
.input-wrap input { width: 100%; min-width: 0; padding: .9rem .8rem; border: 0; outline: 0; color: var(--ink); background: transparent; }
.input-wrap input::placeholder { color: #9fb3c8; }
.password-toggle { align-self: stretch; padding: 0 1rem; border: 0; color: #627d98; background: transparent; }
.password-toggle:hover { color: var(--mint-dark); }
.login-alert { display: flex; align-items: flex-start; gap: .6rem; margin: .2rem 0 1rem; padding: .8rem .9rem; border: 1px solid #ffd1d1; border-radius: 11px; color: #a61b1b; background: #fff4f4; font-size: .86rem; }
.login-button { width: 100%; min-height: 54px; display: flex; align-items: center; justify-content: center; gap: .7rem; margin-top: 1.65rem; border: 0; border-radius: 13px; color: white; background: linear-gradient(135deg, #159c78, #087f6a); box-shadow: 0 12px 25px rgba(8,127,106,.24); font-weight: 750; transition: transform .2s, box-shadow .2s, opacity .2s; }
.login-button:not(:disabled):hover { transform: translateY(-2px); box-shadow: 0 16px 30px rgba(8,127,106,.3); }
.login-button:disabled { opacity: .72; cursor: wait; }
.security-note { margin: 1.15rem 0 0; text-align: center; color: #829ab1; font-size: .76rem; }
.security-note i { margin-right: .35rem; color: var(--mint-dark); }
.mobile-brand { display: none; }
@media (max-width: 820px) { .login-page { padding: 1rem; } .login-shell { min-height: auto; grid-template-columns: 1fr; } .brand-panel { display: none; } .auth-panel { min-height: calc(100vh - 2rem); padding: 2rem; } .mobile-brand { display: flex; align-items: center; position: absolute; top: 2rem; left: 2rem; padding: .35rem .7rem; border-radius: 12px; background: #0d665f; box-shadow: 0 8px 18px rgba(8,127,106,.18); } .mobile-brand img { display: block; width: 9rem; height: auto; } .login-card { padding-top: 4rem; } }
@media (max-width: 420px) { .auth-panel { padding: 1.35rem; } .mobile-brand { top: 1.35rem; left: 1.35rem; } .login-card h2 { font-size: 1.75rem; } }
</style>
