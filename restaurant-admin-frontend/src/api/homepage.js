import api from "@/api/axios";

const unwrap = (response) => response.data?.data ?? response.data ?? {};

export const homepageApi = {
  async getHomepage() {
    return unwrap(await api.get("/api/homepage"));
  },

  async getAdminHomepage() {
    return unwrap(await api.get("/api/admin/homepage"));
  },

  async updateAdminHomepage(payload) {
    return unwrap(await api.put("/api/admin/homepage", payload));
  },
};
