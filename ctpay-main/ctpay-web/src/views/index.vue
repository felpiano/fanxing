<template>
  <div class="app-container home">
    <NoData v-if="identity === 1"></NoData>
    <MerchantPage v-if="identity === 5"></MerchantPage>
    <AgentPage v-if="identity === 3"></AgentPage>
  </div>
</template>

<script>
import NoData from '@/components/NoData/index.vue';
import MerchantPage from '@/views/home/merchant.vue';
import AgentPage from '@/views/home/agent.vue';

export default {
  name: "Index",
  components: {
    NoData,
    MerchantPage,
    AgentPage
  },
  data() {
    return {
      identity: 0
    };
  },
  mounted() {
    const showGoogle = this.$store.getters.showGoogle;
    if (showGoogle) {
      this.identity = this.$store.getters.identity
    }
  },
  methods: {
    
  }
};
</script>

<style scoped lang="scss">
.home {
  position: relative;
  width: 100%;
  height: 100%;
  background: #f3f3f3;
  padding: 10px;
}
</style>

