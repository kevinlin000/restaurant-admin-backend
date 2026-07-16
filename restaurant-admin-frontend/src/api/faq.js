import api from "@/api/axios";
import { demoFaqs } from "@/api/demoData";

const unwrap = (response) => response.data?.data ?? response.data ?? [];

export const faqApi = {
  async getPublishedFaqs(category) {
    const params = category ? { category } : {};
    try {
      const faqs = unwrap(await api.get("/api/faqs", { params }));
      if (Array.isArray(faqs)) return faqs;
      return category ? demoFaqs.filter((faq) => faq.category === category) : demoFaqs;
    } catch {
      return category ? demoFaqs.filter((faq) => faq.category === category) : demoFaqs;
    }
  },

  async searchFaqs(query) {
    try {
      const faqs = unwrap(await api.get("/api/faqs/search", { params: { q: query } }));
      if (Array.isArray(faqs)) return faqs;
    } catch {
    }
    const keyword = `${query || ""}`.trim().toLowerCase();
    if (!keyword) return demoFaqs;
    return demoFaqs.filter((faq) =>
      `${faq.question} ${faq.answer} ${faq.keywords || ""}`.toLowerCase().includes(keyword),
    );
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
