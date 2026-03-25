class TokenStore:
    def __init__(self) -> None:
        self.refresh_tokens: dict[str, str] = {}


store = TokenStore()
