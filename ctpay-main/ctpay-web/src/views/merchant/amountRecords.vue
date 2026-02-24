<!--
** 码商帐变列表--资金明细
-->
<template>
  <div class="app-container">
    <component :is="activeName"></component>
  </div>
</template>

<script>
import AllAmountRecords from './allAmountRecords.vue'
import OneMerchant from './oneMerchant.vue'
export default {
  name: 'amountIndex',
  components: {
    AllAmountRecords,
    OneMerchant
  },
  data() {
    return {
    }
  },
  computed: {
    activeName() {
      console.log(this.$store.getters.merchantLevel)
      if (this.$store.getters.identity === 5 && this.$store.getters.merchantLevel === 1) {
        return 'oneMerchant'
      } else {
        return 'allAmountRecords'
      }
    }
  },
  mounted() {
  },
  methods: {

  }
}
</script>
