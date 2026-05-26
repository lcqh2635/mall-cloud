<template>
  <div>
    <el-button type="primary" @click="handleAdd">新增</el-button>

    <el-table :data="tableData" style="width: 100%; margin-top: 20px">
      <el-table-column v-for="column in columns" :key="column.prop" :prop="column.prop" :label="column.label" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="text" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="form" label-width="100px">
        <el-form-item v-for="column in columns" :key="column.prop" :label="column.label">
          <el-input v-model="form[column.prop]" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { get${entity}List, create${entity}, update${entity}, delete${entity} } from '@/api/${table.entityPath}';
import { ${entity} } from '@/types/${table.entityPath}';

const tableData = ref<${entity}[]>([]);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const form = reactive<Partial<${entity}>>({});
const isEdit = ref(false);

const columns = [
  <#list table.fields as field>
  { prop: '${field.propertyName}', label: '${field.comment!}' },
  </#list>
];

const fetchData = async () => {
  const res = await get${entity}List();
  tableData.value = res.data;
};

const handleAdd = () => {
  dialogTitle.value = '新增';
  isEdit.value = false;
  Object.keys(form).forEach(key => delete form[key]);
  dialogVisible.value = true;
};

const handleEdit = (row: ${entity}) => {
  dialogTitle.value = '编辑';
  isEdit.value = true;
  Object.assign(form, row);
  dialogVisible.value = true;
};

const handleDelete = async (id: number) => {
  await delete${entity}(id);
  fetchData();
};

const handleSubmit = async () => {
  if (isEdit.value) {
    await update${entity}(form);
  } else {
    await create${entity}(form);
  }
  dialogVisible.value = false;
  fetchData();
};

fetchData();
</script>