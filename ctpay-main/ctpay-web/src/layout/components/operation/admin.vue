<template>
  <div class="flex-center admin-nav">
    <el-button
      v-hasPermi="['system:order:createTest']"
      type="success" size="small"
      @click="handleTestOrder">
      测试收银台
    </el-button>

    <!--创建测试单-->
    <createTestOrder ref="createTestOrder"></createTestOrder>
  </div>
</template>

<script>
import createTestOrder from '@/views/order/components/createTestOrder.vue'

export default {
  components: {
    createTestOrder
  },
  data() {
    return {
    }
  },
  mounted() {
  },
  methods: {
    // 创建测试订单
    handleTestOrder() {
      this.$refs.createTestOrder.openDialog()
    }
  }
}
</script>

<style scoped lang="scss">
  .admin-nav {

  }
</style>
