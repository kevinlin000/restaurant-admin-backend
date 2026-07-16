import api from "@/api/axios";
import { demoHomepage } from "@/api/demoData";

const unwrap = (response) => response.data?.data ?? response.data ?? {};

export const homepageApi = {
  async getHomepage() {
    try {
      const setting = unwrap(await api.get("/api/homepage"));
      return setting && typeof setting === "object" && !Array.isArray(setting)
        ? setting
        : demoHomepage;
    } catch {
      return demoHomepage;
    }
  },

  async getAdminHomepage() {
    return unwrap(await api.get("/api/admin/homepage"));
  },

  async updateAdminHomepage(payload) {
    return unwrap(await api.put("/api/admin/homepage", payload));
  },
};
