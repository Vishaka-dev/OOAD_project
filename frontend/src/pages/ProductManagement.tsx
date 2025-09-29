import { ProductManagementTable } from "@/components/ProductManagementTable";
import { Header } from "@/components/Header";

const ProductManagement = () => {
  return (
    <div className="min-h-screen bg-background">
      <Header />
      <div className="container max-w-screen-xl mx-auto px-4 py-8">
        <ProductManagementTable />
      </div>
    </div>
  );
};

export default ProductManagement;
