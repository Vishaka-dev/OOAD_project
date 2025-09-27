import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

interface SortSelectProps {
  value: "name" | "price" | "newest";
  onChange: (value: "name" | "price" | "newest") => void;
}

export function SortSelect({ value, onChange }: SortSelectProps) {
  const sortOptions = [
    { value: "name", label: "Name A-Z" },
    { value: "price", label: "Price: Low to High" },
    { value: "newest", label: "Newest First" },
  ];

  return (
    <Select value={value} onValueChange={onChange}>
      <SelectTrigger className="w-[180px]">
        <SelectValue placeholder="Sort by" />
      </SelectTrigger>
      <SelectContent>
        {sortOptions.map((option) => (
          <SelectItem key={option.value} value={option.value}>
            {option.label}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
