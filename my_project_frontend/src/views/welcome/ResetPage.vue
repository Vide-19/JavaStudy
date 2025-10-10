<script setup>

import {computed, reactive, ref} from "vue";
import {Lock, Message, SuccessFilled} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import {get, post} from "@/net/index.js";
import router from "@/router/index.js";

const active = ref(0)

const form = reactive({
  email: '',
  code: '',
  password: '',
  password_repeat: ''
})

const timer = ref(null)

const coldTime = ref(0)

const formRef = ref()

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('密码前后不一致'))
  } else {
    callback()
  }
}

const rule = {
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在6-20字符之间', trigger: ['blur', 'change'] }
  ],
  password_repeat: [
    { validator: validatePassword, trigger: ['blur', 'change'] }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入合法邮箱', trigger: ['blur', 'change'] }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
  ]
}

function askCode() {
  if (!isEmailValid.value) {
    ElMessage.warning('请输入正确的邮箱')
    return
  }
  // ✅ 如果已有倒计时，阻止重复发送
  if (coldTime.value > 0) return
  // ✅ 发送请求获取验证码
  get(`/api/auth/ask-code?email=${form.email}&type=register`, () => {
    ElMessage.success(`验证码已发送至邮箱：${form.email}`)
    coldTime.value = 60 // 设置倒计时60秒
    // ✅ 启动定时器，并保存引用
    timer.value = setInterval(() => {
      coldTime.value--
      if (coldTime.value <= 0) {
        clearInterval(timer.value)
        timer.value = null
      }
    }, 1000)
  }, (message) => {
    ElMessage.warning(message)
    coldTime.value = 0
    if (timer.value) {
      clearInterval(timer.value)
      timer.value = null
    }
  })
}

const isEmailValid = computed(() => /^[\w\.-]+@[\w\.-]+\.\w+$/.test(form.email))

function confirmReset() {
  formRef.value.validate((valid) => {
    if (valid) {
      post('api/auth/reset-confirm', {
        email: form.email,
        code: form.code
      }, () => {
        active.value++
      })
    }
  })
}

function doReset() {
  formRef.value.validate((valid) => {
    if (valid) {
      post('api/auth/reset-password', {...form}, () => {
        ElMessage.success('密码重置成功，请重新登录')
        router.push('/')
      })
    }
  })
}

</script>

<template>
  <div style="text-align: center">
    <div style="margin-top: 30px">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="验证电子邮件"/>
        <el-step title="重新设定密码"/>
      </el-steps>
    </div>
    <div v-if="active === 0" style="margin: 0 30px">
      <div style="margin-top: 80px">
        <div style="font-size: 25px; font-weight: bold">重置密码</div>
        <div style="font-size: 15px; color: gray">请输入需要重置密码的邮箱</div>
      </div>
      <div style="margin-top: 50px">
        <el-form :model="form" :rules="rule" ref="formRef">
          <el-form-item prop="email">
            <el-input v-model="form.email" type="text" placeholder="电子邮件地址">
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="code">
            <el-row :gutter="10" style="width: 100%">
              <el-col :span="17">
                <el-input v-model="form.code" maxlength="6" type="text" placeholder="请输入验证码">
                  <template #prefix>
                    <el-icon><SuccessFilled /></el-icon>
                  </template>
                </el-input>
              </el-col>
              <el-col :span="5">
                <el-button @click="askCode" :disabled="!isEmailValid || coldTime" type="success">
                  {{ coldTime > 0 ? `请等候${coldTime}秒` : '获取验证码'}}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
        </el-form>
      </div>
      <div style="margin-top: 50px">
        <el-button @click="confirmReset" type="success" style="width: 270px">开始重置</el-button>
      </div>
    </div>
    <div v-if="active === 1" style="margin: 0 30px">
      <div style="margin-top: 50px">
        <div style="font-size: 25px; font-weight: bold">重置密码</div>
        <div style="font-size: 15px; color: gray">请输入新密码</div>
      </div>
      <div style="margin-top: 50px">
        <el-form :model="form" :rules="rule" ref="formRef">
          <el-form-item prop="password">
            <el-input v-model="form.password" maxlength="20" type="password" placeholder="新密码">
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password_repeat">
            <el-input v-model="form.password_repeat" maxlength="20" type="password" placeholder="重复新密码">
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <div style="margin-top: 50px">
        <el-button @click="doReset" type="success" style="width: 270px">立即重置</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>