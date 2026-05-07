// import request from "@/utils/request.ts";
// import {LoginData, ResponseData} from "@/types";
//
// // 定义 UserInfo 类型
// interface UserInfo {
//     id: number,
//     name: string,
//     age: number,
//     address: string
// }
//
//
// // params 必须是一个简单对象或 URLSearchParams 对象
// // axios 会自动将 params 中的这些参数转换为查询字符串添加到请求的 URL 中，
// // 最终的请求 URL 将会是类似 https://api.example.com/data?id=12345&category=programming 这样的形式。
// export const select = async (params: any) => {
//     return await request.get<ResponseData<UserInfo>>(
//         "/userInfo",
//         {
//             params: params
//         }
//     )
// };
//
// // 如果需要路径拼接参数，则需要使用 ${} 占位符来动态取参。同时还需要使用反引符
// // 不过号在 IDEA 能够在我们使用 ${} 占位符时自动将字符串转变成反引符
// export const selectWithPath = async (query: string) => {
//     return await request.get<ResponseData<UserInfo>>(
//         `/userInfo/${query}`
//     )
// };
//
// export const insert = async (data: LoginData) => {
//     return await request.post(
//         "/login", data,
//     )
// };
//
// export const update = async (data: LoginData) => {
//     return await request.put(
//         "/update", data,
//     )
// };
//
// export const deleteApi = async (params: any) => {
//     return await request.delete(
//         "/delete",
//         {
//             params: params
//         }
//     )
// };
