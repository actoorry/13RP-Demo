import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { accountApi } from '../api/base'
import type { BaseAccount } from '../api/base'

const ACCOUNT_KEY = 'currentAccountId'

/** 账套：多账套切换（数据隔离边界），当前账套持久化 localStorage */
export const useAccountStore = defineStore('account', () => {
  const accounts = ref<BaseAccount[]>([])
  const currentAccountId = ref<number | null>(
    Number(localStorage.getItem(ACCOUNT_KEY)) || null,
  )

  const currentAccount = computed<BaseAccount | null>(
    () => accounts.value.find((a) => a.id === currentAccountId.value) || null,
  )

  function persist() {
    if (currentAccountId.value != null) {
      localStorage.setItem(ACCOUNT_KEY, String(currentAccountId.value))
    } else {
      localStorage.removeItem(ACCOUNT_KEY)
    }
  }

  async function fetchAccounts() {
    const data = await accountApi.list({ page: 1, size: 100 })
    accounts.value =
      data && Array.isArray(data.list) ? data.list : Array.isArray(data) ? data : []
    // 默认选中第一个账套
    if (currentAccountId.value == null && accounts.value.length) {
      currentAccountId.value = accounts.value[0].id ?? null
    }
    persist()
  }

  function setAccount(id: number) {
    currentAccountId.value = id
    persist()
  }

  return { accounts, currentAccountId, currentAccount, fetchAccounts, setAccount }
})
