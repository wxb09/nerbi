import api from './api'

interface UploadResult {
  url: string
  filename: string
}

export const uploadApi = {
  uploadImages: async (files: File[]): Promise<UploadResult[]> => {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    
    const response = await api.post<UploadResult[]>('/upload/images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    return response
  }
}
