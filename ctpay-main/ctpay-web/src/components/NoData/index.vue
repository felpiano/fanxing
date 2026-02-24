<template>
  <div class="no-data-tip">
    <p>暂无数据</p>
  </div>
</template>

<script>
export default {
};
</script>

<style lang="scss" scoped>
  .no-data-tip {
    position: relative;
    width: 100%;
    height: 100%;
    text-align: center;
    > p {
      font-size: 14px;
      width: 100%;
      height: 100%;
      padding-top: 12%;
      color: rgba(0,0,0,.58)
    }
  }
</style>
