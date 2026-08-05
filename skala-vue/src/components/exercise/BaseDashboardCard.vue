<template>
  <div class="base-dashboard-card">
    <header v-if="$slots.header" class="base-dashboard-card__header">
      <slot name="header"></slot>
    </header>

    <slot></slot>

    <footer v-if="$slots.footer" class="base-dashboard-card__footer">
      <slot name="footer"></slot>
    </footer>
  </div>
</template>

<style scoped>
.base-dashboard-card {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background-color: #ffffff;
}

.base-dashboard-card__header,
.base-dashboard-card__footer {
  width: 100%;
}
</style>
