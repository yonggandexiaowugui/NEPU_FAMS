export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

export function formatMoney(value) {
  if (value === null || value === undefined) return '0.00'
  return Number(value).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

export function getStatusText(status, type) {
  const statusMap = {
    asset: {
      IN_USE: '在用',
      IDLE: '闲置',
      BORROWED: '借出',
      REPAIRING: '维修中',
      SCRAPPED: '已报废'
    },
    borrow: {
      PENDING: '待审批',
      APPROVED: '已批准',
      REJECTED: '已拒绝',
      RETURNED: '已归还'
    },
    repair: {
      PENDING: '待处理',
      PROCESSING: '维修中',
      COMPLETED: '已完成',
      CANCELLED: '已取消'
    },
    scrap: {
      PENDING: '待审批',
      APPROVED: '已批准',
      REJECTED: '已拒绝'
    },
    inventory: {
      PENDING: '待开始',
      IN_PROGRESS: '进行中',
      COMPLETED: '已完成'
    }
  }
  return statusMap[type]?.[status] || status
}
