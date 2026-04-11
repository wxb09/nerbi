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
    
    return api.post('/upload/images', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000
    }) as Promise<UploadResult[]>
  }
}
