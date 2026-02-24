<!--
** count-to
-->
<template>
  <div class="count-to">
    <count-to
      :startVal="localStartVal"
      :endVal="localEndVal"
      :duration="duration"
      :decimas="decimas"
      @start="handleStart"
      @complete="handleComplete"
    ></count-to>
  </div>
</template>

<script>
import countTo from 'vue-count-to';

export default {
  name: 'CountTo',
  components: { countTo },
  props: {
    startVal: {
      type: Number,
      default: 0
    },
    endVal: {
      type: [Number, String],
      required: true,
      default: 0
    },
    duration: {
      type: Number,
      default: 3000
    },
    // 保留小数点后几位
    decimas: {
      type: Number,
      default: 0
    },
  },
  data() {
    return {
      localStartVal: this.startVal,
      localEndVal: this.endVal
    };
  },
  watch: {
    startVal(newVal) {
      this.localStartVal = newVal || 0;
    },
    endVal(newVal) {
      this.localEndVal = newVal || 0;
    }
  },
  methods: {
    handleStart() {
      this.$emit('start');
    },
    handleComplete() {
      this.$emit('complete');
    }
  }
}
</script>