<template>
  <div class="login-container">
    <h1>Login Page</h1>
    <a-button type="primary" @click="$router.push('/')">Login (Mock)</a-button>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
}
</style>
