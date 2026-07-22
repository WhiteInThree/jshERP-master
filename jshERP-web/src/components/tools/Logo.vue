<template>
  <div class="logo">
    <a-icon type="inbox" class="logo-mark" />
  </div>
</template>

<script>
  import { mixin } from '@/utils/mixin.js'

  export default {
    name: 'Logo',
    mixins: [mixin],
    props: {
      title: {
        type: String,
        default: window.SYS_TITLE,
        required: false
      },
      showTitle: {
        type: Boolean,
        default: true,
        required: false
      }
    }
  }
</script>
<style lang="less" scoped>
  /*缩小首页布 局顶部的高度*/
  @height: 49px;

  .sider {
    box-shadow: none !important;
    .logo {
      color: #ffffff;
      height: @height !important;
      line-height: @height !important;
      box-shadow: none !important;
      transition: background 300ms;
      background: #002140;
      text-align: center;
      .logo-mark {
        color: rgba(255, 255, 255, 0.88);
        font-size: 21px;
      }

    }

    &.light .logo {
      background-color: @primary-color;
    }
  }
</style>
