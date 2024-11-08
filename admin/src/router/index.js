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
        component: () => import(/* webpackChunkName: "about"*/'../views/main/station.vue')
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
