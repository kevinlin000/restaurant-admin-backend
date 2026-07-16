import api from "@/api/axios";
import { demoNews } from "@/api/demoData";

const unwrap = (response) => response.data?.data ?? response.data ?? [];

export const newsApi = {
  async getPublishedNews() {
    try {
      const articles = unwrap(await api.get("/api/news"));
      return Array.isArray(articles) ? articles : demoNews;
    } catch {
      return demoNews;
    }
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
