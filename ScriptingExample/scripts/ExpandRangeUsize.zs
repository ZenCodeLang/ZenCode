public expand usize .. usize {
    public withBounds(bounds as usize .. usize) as usize .. usize {
        return (from < bounds.from ? bounds.from : from) .. (to > bounds.to ? bounds.to : to);
    }
}