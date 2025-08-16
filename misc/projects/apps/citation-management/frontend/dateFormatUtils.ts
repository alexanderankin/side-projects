const americanDate = new Intl.DateTimeFormat("en-US", {
  month: 'long',
  day: "numeric",
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

export function formatDate(date: Date): string {
  return americanDate.format(date);
}
