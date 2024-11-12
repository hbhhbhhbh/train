import { createRouter, createWebHistory } from 'vue-router'
const routes = [
  {
    path: '/',
    name: 'main',
    component: () => import(/* webpackChunkName: "about" */ '../views/main.vue'),
    meta: {
      loginRequire: true
    },
    children: [
      {
        path: 'welcome',
        component: () => import(/* webpackChunkName: "about"*/'../views/main/welcome.vue')
      },
      {
        path: 'about',
        component: () => import(/* webpackChunkName: "about"*/'../views/main/about.vue')
      },
      {
        path: 'station',
        component: () => import(/* webpackChunkName: "about"*/'../views/main/business/station.vue')
      },
      {
        path: 'train-station',
        component: () => import(/* webpackChunkName: "about"*/'../views/main/business/train-station.vue')
      },
      {
        path: 'train',
        component: () => import(/* webpackChunkName: "about"*/'../views/main/business/train.vue')
      },
      {
        path: 'train-carriage',
        component: () => import(/* webpackChunkName: "about"*/'../views/main/business/train-carriage.vue')
      },
      {
        path: 'train-seat',
        component: () => import(/* webpackChunkName: "about"*/'../views/main/business/train-seat.vue')
      }
      ,
      {
        path: 'batch/job',
        component: () => import(/* webpackChunkName: "about"*/'../views/main/batch/job.vue')
      }
    ]
  },
  {
    path: '',
    redirect: '/welcome'
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})
export default router
