// // services/modules/user/api.ts
// import type {UserQueryParams} from "@/services/modules/user/user";
//
// export const userApi = {
//     profile: {
//
//     },
//     admin: {
//
//     }
// }
//
//
// /**
//  * 用户服务API
//  */
// export const userService = {
//     /**
//      * 获取用户列表
//      */
//     getUsers(
//         params: UserQueryParams & PaginationParams,
//         config?: CustomRequestConfig
//     ) {
//         return http.get<BaseResponse<PaginationResponse<UserInfo>>>(
//             '/users',
//             { params, ...config }
//         )
//     },
//
//     /**
//      * 获取用户详情
//      */
//     getUserById(id: string, config?: CustomRequestConfig) {
//         return http.get<BaseResponse<UserInfo>>(
//             `/users/${id}`,
//             config
//         )
//     },
//
//     /**
//      * 创建用户
//      */
//     createUser(data: UserFormData, config?: CustomRequestConfig) {
//         return http.post<BaseResponse>('/users', data, config)
//     },
//
//     /**
//      * 更新用户
//      */
//     updateUser(id: string, data: Partial<UserFormData>, config?: CustomRequestConfig) {
//         return http.put<BaseResponse>(`/users/${id}`, data, config)
//     },
//
//     /**
//      * 删除用户
//      */
//     deleteUser(id: string, config?: CustomRequestConfig) {
//         return http.delete<BaseResponse>(`/users/${id}`, config)
//     },
//
//     /**
//      * 批量删除用户
//      */
//     batchDeleteUsers(ids: string[], config?: CustomRequestConfig) {
//         return http.delete<BaseResponse>('/users/batch', {
//             data: { ids },
//             ...config
//         })
//     },
//
//     /**
//      * 上传用户头像
//      */
//     uploadAvatar(file: File, onProgress?: (progress: number) => void) {
//         const formData = new FormData()
//         formData.append('file', file)
//
//         return http.post<BaseResponse<{ url: string }>>(
//             '/users/avatar',
//             formData,
//             {
//                 headers: {
//                     'Content-Type': 'multipart/form-data'
//                 },
//                 onUploadProgress: (progressEvent) => {
//                     if (onProgress && progressEvent.total) {
//                         onProgress(Math.round((progressEvent.loaded * 100) / progressEvent.total))
//                     }
//                 }
//             }
//         )
//     }
// }
//
// export type UserService = typeof userService