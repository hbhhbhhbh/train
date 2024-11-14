<template>
  <div class="order-train">
  <span class="order-train-main">{{dailyTrainTicket.date}}</span>&nbsp;
  <span class="order-train-main">{{dailyTrainTicket.trainCode}}</span>次&nbsp;
  <span class="order-train-main">{{dailyTrainTicket.start}}</span>站
  <span class="order-train-main">({{dailyTrainTicket.startTime}})</span>&nbsp;
  <span class="order-train-main">——</span>&nbsp;
  <span class="order-train-main">{{dailyTrainTicket.end}}</span>站
  <span class="order-train-main">({{dailyTrainTicket.endTime}})</span>&nbsp;
  <div class="order-train-ticket">
    <span v-for="item in seatTypes" :key="item.type">
      <span>{{item.desc}}</span>：
      <span class="order-train-ticket-main">{{item.price}}￥</span>&nbsp;
      <span class="order-train-ticket-main">{{item.count}}</span>&nbsp;张票&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    </span>
  </div>

  </div>
  <a-divider> </a-divider>
  <b>勾选购票乘客</b>
  <a-checkbox-group v-model:value="passengerChecks" :options="passengerOptions"/>
  <div class="order-tickets">
    <a-row class="order-tickets-header" v-if="tickets.length > 0">
      <a-col :span="2">乘客</a-col>
      <a-col :span="6">身份证</a-col>
      <a-col :span="4">票种</a-col>
      <a-col :span="4">座位类型</a-col>
    </a-row>
    <a-row class="order-tickets-row" v-for="ticket in tickets" :key="ticket.passengerId">
      <a-col :span="2">{{ticket.passengerName}}</a-col>
      <a-col :span="6">{{ticket.passengerIdCard}}</a-col>
      <a-col :span="4">
        <a-select v-model:value="ticket.passengerType" style="width: 100%"
                 >
          <a-select-option v-for="item in PASSENGER_TYPE_ARRAY" :key="item.code" :value="item.code">
            {{item.desc}}
          </a-select-option>
        </a-select>
      </a-col>
      <a-col :span="4">
        <a-select v-model:value="ticket.seatTypeCode" style="width: 100%"

        >
          <a-select-option v-for="item in seatTypes" :key="item.code" :value="item.code">
            {{item.desc}}
          </a-select-option>
        </a-select>
      </a-col>
    </a-row>
  </div>
  <div v-if="tickets.length > 0">
    <a-button type="primary" size="large" @click="finishCheckPassenger">提交订单</a-button>
  </div>

  <a-modal v-model:visible="visible" title="请核对以下信息"
           style="top: 50px; width: 800px"
           ok-text="确认" cancel-text="取消"
           @ok="handleOk">
    <div class="order-tickets">
      <a-row class="order-tickets-header" v-if="tickets.length > 0">
        <a-col :span="3">乘客</a-col>
        <a-col :span="15">身份证</a-col>
        <a-col :span="3">票种</a-col>
        <a-col :span="3">座位类型</a-col>
      </a-row>
      <a-row class="order-tickets-row" v-for="ticket in tickets" :key="ticket.passengerId">
        <a-col :span="3">{{ticket.passengerName}}</a-col>
        <a-col :span="15">{{ticket.passengerIdCard}}</a-col>
        <a-col :span="3">
          <span v-for="item in PASSENGER_TYPE_ARRAY" :key="item.code">
            <span v-if="item.code === ticket.passengerType">
              {{item.desc}}
            </span>
          </span>
        </a-col>
        <a-col :span="3">
          <span v-for="item in seatTypes" :key="item.code">
            <span v-if="item.code === ticket.seatTypeCode">
              {{item.desc}}
            </span>
          </span>
        </a-col>
      </a-row>

    </div>
  </a-modal>
  购票列表：{{tickets}}
</template>
<script>
import {defineComponent, onMounted, ref, watch} from 'vue';
import {notification} from "ant-design-vue";
import axios from "axios";

export default defineComponent({
  name: "order-view",
  setup() {
    const passengers=ref([]);
    const passengerOptions=ref([]);
    const passengerChecks=ref([]);
    const dailyTrainTicket=SessionStorage.get(SESSION_ORDER);

    const SEAT_TYPE = window.SEAT_TYPE;
    console.log(SEAT_TYPE)
    // 本车次提供的座位类型seatTypes，含票价，余票等信息，例：
    // {
    //   type: "YDZ",
    //   code: "1",
    //   desc: "一等座",
    //   count: "100",
    //   price: "50",
    // }
    // 关于SEAT_TYPE[KEY]：当知道某个具体的属性xxx时，可以用obj.xxx，当属性名是个变量时，可以使用obj[xxx]
    const seatTypes = [];
    for (let KEY in SEAT_TYPE) {
      let key = KEY.toLowerCase();
      if (dailyTrainTicket[key] >= 0) {
        seatTypes.push({
          type: KEY,
          code: SEAT_TYPE[KEY]["code"],
          desc: SEAT_TYPE[KEY]["desc"],
          count: dailyTrainTicket[key],
          price: dailyTrainTicket[key + 'Price'],
        })
      }
    }
    console.log("本车次提供的座位：", seatTypes);

    // 购票列表，用于界面展示，并传递到后端接口，用来描述：哪个乘客购买什么座位的票
    // {
    //   passengerId: 123,
    //   passengerType: "1",
    //   passengerName: "张三",
    //   passengerIdCard: "12323132132",
    //   seatTypeCode: "1",
    //   seat: "C1"
    // }
    const tickets = ref([]);
    const PASSENGER_TYPE_ARRAY = window.PASSENGER_TYPE_ARRAY;
    const visible = ref(false);

    // 勾选或去掉某个乘客时，在购票列表中加上或去掉一张表
    watch(() => passengerChecks.value,
        (newVal, oldVal) => {
          console.log("勾选乘客发生变化", newVal, oldVal);

          // 处理新增的乘客
          newVal.forEach((newItem) => {
            const exists = oldVal.some((oldItem) => oldItem.id === newItem.id); // 检查乘客是否已经在旧的列表中
            if (!exists) {
              // 如果乘客没有出现在旧列表中，表示是新增的
              tickets.value.push({
                passengerId: newItem.id,
                passengerType: newItem.type,
                seatTypeCode: seatTypes[0].code, // 默认座位类型
                passengerName: newItem.name,
                passengerIdCard: newItem.idCard
              });
            }
          });

          //处理被移除的乘客
          if(Tool.isNotEmpty(oldVal))
          oldVal.forEach((oldItem) => {
            const removed = !newVal.some((newItem) => newItem.id === oldItem.id); // 检查乘客是否被移除
            if (removed) {
              // 如果乘客在新列表中不存在，表示被移除了
              tickets.value = tickets.value.filter((ticket) => ticket.passengerId !== oldItem.id); // 从购票列表中移除
            }
          });
        },
        { immediate: true }
    );

    // const updatePassengerCheck = (passengerId, field, value) => {
    //   const passenger1 =passengerChecks.value.find(item => item.id === passengerId);
    //   if (passenger1) {
    //     // 更新指定字段
    //     passenger1[field] = value;
    //   }
    // };
    const handleQueryPassenger=()=>{
      axios.get("/member/passenger/query-mine").then((response) =>{
        let data = response.data;
        if (data.success) {
          passengers.value = data.content;

          passengers.value.forEach((item)=>passengerOptions.value.push({
            label: item.name,
            value: item,
          }))
          // passengerOptions.value.forEach((item) => item.seatType= seatTypes[0].code);
        } else {
          notification.error({description:data.message});
        }
      });
    };
    const finishCheckPassenger = () => {
      console.log("购票列表：", tickets.value);

      if (tickets.value.length > 5) {
        notification.error({description: '最多只能购买5张车票'});
        return;
      }
      visible.value=true;
    }
          onMounted(() => {
      handleQueryPassenger();
    });
    return {
      dailyTrainTicket,
      seatTypes,
      passengers,
      handleQueryPassenger,
      passengerOptions,
      passengerChecks,
      tickets,
      PASSENGER_TYPE_ARRAY,
      visible,
      finishCheckPassenger,
      // updatePassengerCheck,
    };
  },
});
</script>
<style>
.order-train .order-train-main {
  font-size: 18px;
  font-weight: bold;
}
.order-train .order-train-ticket {
  margin-top: 15px;
}
.order-train .order-train-ticket .order-train-ticket-main {
  color: red;
  font-size: 18px;
}

.order-tickets {
  margin: 10px 0;
}
.order-tickets .ant-col {
  padding: 5px 10px;
}
.order-tickets .order-tickets-header {
  background-color: cornflowerblue;
  border: solid 1px cornflowerblue;
  color: white;
  font-size: 16px;
  padding: 5px 0;
}
.order-tickets .order-tickets-row {
  border: solid 1px cornflowerblue;
  border-top: none;
  vertical-align: middle;
  line-height: 30px;
}

.order-tickets .choose-seat-item {
  margin: 5px 5px;
}
</style>
