import api from "@/api/axios";

const unwrap = (response) => response.data?.data ?? response.data ?? [];

export const faqApi = {
  async getPublishedFaqs(category) {
    const params = category ? { category } : {};
    return unwrap(await api.get("/api/faqs", { params }));
  },

  async searchFaqs(query) {
    return unwrap(await api.get("/api/faqs/search", { params: { q: query } }));
  },

  async getAdminFaqs() {
    return unwrap(await api.get("/api/admin/faqs"));
  },

  async getSearchAnalytics() {
    return unwrap(await api.get("/api/admin/faqs/search-logs"));
  },

  async createFaq(payload) {
    return unwrap(await api.post("/api/admin/faqs", payload));
  },

  async updateFaq(faqId, payload) {
    return unwrap(await api.put(`/api/admin/faqs/${faqId}`, payload));
  },

  async deleteFaq(faqId) {
    return unwrap(await api.delete(`/api/admin/faqs/${faqId}`));
  },
};
