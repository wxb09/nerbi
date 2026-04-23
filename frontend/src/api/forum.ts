import api from './api'

export interface PostList {
  id: number
  type: string
  title: string | null
  content: string
  images: string | null
  tags: string | null
  likeCount: number
  commentCount: number
  viewCount: number
  status: string
  likedByMe: boolean
  author: {
    id: number
    nickname: string
    avatar: string | null
    building: string | null
  }
  communityId: number | null
  communityName: string | null
  createdAt: string
}

export interface PostDetail {
  id: number
  type: string
  title: string | null
  content: string
  images: string[]
  tags: string[]
  likeCount: number
  commentCount: number
  viewCount: number
  status: string
  likedByMe: boolean
  author: {
    id: number
    nickname: string
    avatar: string | null
    building: string | null
    bio: string | null
  }
  communityId: number | null
  communityName: string | null
  createdAt: string
  updatedAt: string
}

export interface CreatePostRequest {
  type?: string
  title?: string
  content: string
  images?: string[]
  tags?: string[]
  communityId?: number
}

export interface CommentData {
  id: number
  content: string
  likeCount: number
  likedByMe: boolean
  author: {
    id: number
    nickname: string
    avatar: string | null
    building: string | null
  }
  parentId: number | null
  createdAt: string
}

export interface CreateCommentRequest {
  postId: number
  parentId?: number
  content: string
}

export interface LikeStatus {
  liked: boolean
  likeCount: number
}

export interface Announcement {
  id: number
  title: string
  content: string
  type: string
  communityId: number | null
  communityName: string | null
  createdAt: string
}

export const forumApi = {
  getPosts: (params: {
    communityId?: number
    type?: string
    sort?: string
    page?: number
    size?: number
  }) => api.get('/forum/posts', { params }),

  getPostById: (id: number) => api.get(`/forum/posts/${id}`),

  createPost: (data: CreatePostRequest) => api.post('/forum/posts', data),

  updatePost: (id: number, data: CreatePostRequest) => api.put(`/forum/posts/${id}`, data),

  deletePost: (id: number) => api.delete(`/forum/posts/${id}`),

  getMyPosts: (params: { page?: number; size?: number }) =>
    api.get('/forum/posts/my', { params }),

  getComments: (postId: number, params: { page?: number; size?: number }) =>
    api.get(`/forum/comments/post/${postId}`, { params }),

  getReplies: (commentId: number, params: { page?: number; size?: number }) =>
    api.get(`/forum/comments/${commentId}/replies`, { params }),

  createComment: (data: CreateCommentRequest) => api.post('/forum/comments', data),

  deleteComment: (id: number) => api.delete(`/forum/comments/${id}`),

  toggleLike: (targetType: string, targetId: number) =>
    api.post('/forum/likes/toggle', { targetType, targetId }),

  checkLike: (targetType: string, targetId: number) =>
    api.get('/forum/likes/check', { params: { targetType, targetId } }),

  getAnnouncements: (communityId?: number) =>
    api.get<Announcement[]>('/announcements', { params: { communityId } }),

  getAnnouncementById: (id: number) =>
    api.get<Announcement>(`/announcements/${id}`),

  reportContent: (data: { targetType: string; targetId: number; reason: string }) =>
    api.post('/reports', data),
}
