import api from "@/api/axios";
import { demoFaqs } from "@/api/demoData";

const unwrap = (response) => response.data?.data ?? response.data ?? [];
const categoryLabels = {
  RESERVATION: "訂位",
  DEPOSIT: "訂金",
  ORDER: "點餐",
  PAYMENT: "付款",
  STORE: "門市",
  MEMBER: "會員",
  SERVICE: "服務",
};
const withCategoryLabel = (faq) => ({
  ...faq,
  categoryLabel: faq.categoryLabel || categoryLabels[faq.category] || faq.category || "FAQ",
});

const searchDemoFaqs = (query) => {
  const keyword = `${query || ""}`.trim().toLowerCase();
  const source = demoFaqs.map(withCategoryLabel);
  if (!keyword) {
    return { results: [], suggestions: source.filter((faq) => [1, 5, 7, 10].includes(faq.faqId)).slice(0, 4) };
  }
  const terms = keyword.split(/\s+/).filter(Boolean);
  const scored = source
    .map((faq) => {
      const question = `${faq.question}`.toLowerCase();
      const keywords = `${faq.keywords || ""}`.toLowerCase();
      const answer = `${faq.answer}`.toLowerCase();
      const score = terms.reduce((sum, term) => {
        if (question.includes(term)) return sum + 5;
        if (keywords.includes(term)) return sum + 3;
        if (answer.includes(term)) return sum + 1;
        return sum;
      }, 0);
      return { faq, score };
    })
    .filter((item) => item.score > 0)
    .sort((a, b) => b.score - a.score)
    .map((item) => item.faq)
    .slice(0, 5);
  return { results: scored, suggestions: source.slice(0, 4) };
};

export const faqApi = {
  async getPublishedFaqs(category) {
    const params = category ? { category } : {};
    try {
      const faqs = unwrap(await api.get("/api/faqs", { params }));
      if (Array.isArray(faqs)) return faqs;
      return category ? demoFaqs.filter((faq) => faq.category === category) : demoFaqs;
    } catch {
      const faqs = category ? demoFaqs.filter((faq) => faq.category === category) : demoFaqs;
      return faqs.map(withCategoryLabel);
    }
  },

  async searchFaqs(query) {
    try {
      const faqs = unwrap(await api.get("/api/faqs/search", { params: { q: query } }));
      if (Array.isArray(faqs) && faqs.length) return { results: faqs.map(withCategoryLabel), suggestions: [] };
      if (faqs && typeof faqs === "object" && (Array.isArray(faqs.results) || Array.isArray(faqs.suggestions))) {
        return {
          results: (faqs.results || []).map(withCategoryLabel),
          suggestions: (faqs.suggestions || []).map(withCategoryLabel),
        };
      }
    } catch {
    }
    return searchDemoFaqs(query);
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
