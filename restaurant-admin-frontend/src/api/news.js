import api from "@/api/axios";

const unwrap = (response) => response.data?.data ?? response.data ?? [];

export const newsApi = {
  async getPublishedNews() {
    return unwrap(await api.get("/api/news"));
  },

  async getAdminNews() {
    return unwrap(await api.get("/api/admin/news"));
  },

  async createNews(payload) {
    return unwrap(await api.post("/api/admin/news", payload));
  },

  async updateNews(newsId, payload) {
    return unwrap(await api.put(`/api/admin/news/${newsId}`, payload));
  },

  async deleteNews(newsId) {
    return unwrap(await api.delete(`/api/admin/news/${newsId}`));
  },
};
