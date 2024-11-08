<template>
  <p>
    <a-space>
      <a-button type="primary" @click="handleQuery()">刷新</a-button>

      <a-button type="primary" @click="showModal">新增</a-button>

    </a-space>
  </p>
  <a-table :columns="columns"
           :dataSource="passengers"
           :pagination="pagination"
           @change="handleTableChange"
            :loading="loading">
     <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'operation'">
        <a-space>
          <a-popconfirm title="确定删除吗?" @confirm="onDelete(record)"
          ok-text="确定" cancel-text="取消">
            <a style="color:red">删除</a>
        </a-popconfirm>
       <a @click="onEdit(record)">编辑</a> </a-space>
      </template></template>
    </a-table>
  <a-modal v-model:visible="visible" title="乘车人" @ok="handleOk" ok-text="确认" cancel-text="取消">
    <a-form :model="passenger" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
      <a-form-item label="姓名">
        <a-input v-model:value="passenger.name" />
      </a-form-item>
      <a-form-item label="身份证">
        <a-input v-model:value="passenger.idCard" />
      </a-form-item>
      <a-form-item label="旅客类型">
        <a-select v-model:value="passenger.type">
          <a-select-option v-for="item in PASSENGER_TYPE_ARRAY" :key="item.code" :value="item.code">
            {{item.desc}}
          </a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script>
import {defineComponent, ref, onMounted,} from 'vue';
import { notification } from 'ant-design-vue';
import axios from 'axios';

export default defineComponent({
  setup() {
    const PASSENGER_TYPE_ARRAY = [
      {code:'ADULT',desc:'成人'},
      {code:'CHILD',desc:'儿童'},
      {code:'INFANT',desc:'婴儿'},
    ];

    const visible = ref(false);
    let passenger = ref({
      id: undefined,
      memberId: undefined,
      name: undefined,
      idCard: undefined,
      type: undefined,
      createTime: undefined,
      updateTime: undefined,
    });

    let loading=ref(false);
    let pagination = ref(
        {
          total:0,
          current:1,
          pageSize:2,
        }
    )
    let passengers=ref([]);
    const columns= [
      {
        title: '姓名',
        dataIndex: 'name',
        key: 'name',
      },
      {
        title: '身份证',
        dataIndex: 'idCard',
        key: 'idCard',
      },
      {
        title: '类型',
        dataIndex: 'type',
        key: 'type',
      },
      {
        title: '操作',
        dataIndex: 'operation',

      },
    ];
    const handleQuery = (param) => {
      if(!param)
      {
        param={page:1,size:pagination.value.pageSize};
      }
      loading.value=true;
      axios.get("/member/passenger/query-list", {
        params: {
          page: param.page,
          size: param.size
        }
      }).then((response) => {
        loading.value=false;
        let data = response.data;
        if (data.success) {
          passengers.value = data.content.list;
          pagination.value.current=param.page;
          pagination.value.total= data.content.total;
        } else {
          notification.error({description: data.message});
        }
      });
    };
    const handleTableChange = (pagination) => {
      handleQuery({page: pagination.current, size: pagination.pageSize});
    }
    onMounted(() => {
      handleQuery({page: 1, size: pagination.value.pageSize});
    }
    );
    const onEdit = (record) => {
      passenger.value = window.Tool.copy(record);
      visible.value = true;

    }
    const onDelete = (record) => {
      axios.delete("/member/passenger/delete/"+record.id, ).then((response) => {
        let data = response.data;
        if (data.success) {
          notification.success({ description: '删除成功！' });
          handleQuery({page: pagination.value.current, size: pagination.value.pageSize});
        } else {
          notification.error({ description: data.message });
        }
      });
    }
    const showModal = () => {
      passenger.value={};
      visible.value = true;
    };
    const handleOk = () => {
      console.log(passenger);
      axios.post('/member/passenger/save', passenger.value).then((response) => {
        let data = response.data;
        console.log(data);
        if (data.success) {
          notification.success({ description: '保存成功！' });
          visible.value = false;
          handleQuery({page: pagination.value.current, size: pagination.value.pageSize});
        } else {
          notification.error({ description: data.message });
        }
      });
    };

    return {
      visible,
      showModal,
      handleOk,
      passenger,
      passengers,
      columns,
      pagination,
      handleQuery,
      handleTableChange,
      loading,
      onEdit,
      onDelete,
      PASSENGER_TYPE_ARRAY
    };
  },
});
</script>

<style></style>
<script setup>
</script>