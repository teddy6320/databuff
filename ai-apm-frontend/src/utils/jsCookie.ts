import Cookies from 'js-cookie';

export const TokenKey = 'DATABUFF-Admin-Token';
export const CidKey = 'DATABUFF-Admin-Cid';
export const DsiKey = 'DATABUFF-DSI';
export const OiKey = 'DATABUFF-OI';
export const DbhKey = 'DATABUFF-DBH';
export const DbApikey = 'DATABUFF-DBA';
export const AccountGroupIdKey = 'DATABUFF-AGI';

export function getToken() {
  return Cookies.get(TokenKey);
}

export function getCid() {
  return Cookies.get(CidKey);
}

export function setTokenAndCid(token: string, cid: string) {
  Cookies.set(TokenKey, token);
  Cookies.set(CidKey, cid);
}

export function updateToken(token: string) {
  if (token) {
    Cookies.set(TokenKey, token);
  }
}

export function removeTokenAndCid() {
  Cookies.remove(TokenKey);
  Cookies.remove(CidKey);
}

export function getDsi() {
  return Cookies.get(DsiKey) || window.localStorage.getItem(DsiKey);
}

export function setDsi(dsi: string) {
  window.localStorage.setItem(DsiKey, dsi);
  return Cookies.set(DsiKey, dsi);
}

export function getOi() {
  return Cookies.get(OiKey) || window.localStorage.getItem(OiKey);
}

export function setOi(oi: string) {
  window.localStorage.setItem(OiKey, oi);
  return Cookies.set(OiKey, oi);
}

export function getDbh() {
  return Cookies.get(DbhKey) || window.localStorage.getItem(DbhKey);
}

export function setDbh(dbh: string) {
  window.localStorage.setItem(DbhKey, dbh);
  return Cookies.set(DbhKey, dbh);
}

export function getDba() {
  return Cookies.get(DbApikey) || window.localStorage.getItem(DbApikey);
}

export function setDba(dba?: string) {
  window.localStorage.setItem(DbApikey, dba || 'NzhEMjlBOTk3MzkxRjk5MjQyMzMzQzI4');
  return Cookies.set(DbApikey, dba);
}

export function getAgi() {
  return Cookies.get(AccountGroupIdKey) || window.localStorage.getItem(AccountGroupIdKey);
}

export function setAgi(accountGroupIds: string) {
  window.localStorage.setItem(AccountGroupIdKey, accountGroupIds);
  return Cookies.set(AccountGroupIdKey, accountGroupIds);
}

export function removeAgi() {
  Cookies.remove(AccountGroupIdKey);
  window.localStorage.removeItem(AccountGroupIdKey);
}

export function getRequestHeaders() {
  if (!getToken() || !getCid()) {
    return null;
  }
  const headers: any = {
    Authorization: getToken(),
    cid: getCid(),
  }
  const agi = getAgi();
  if (agi) {
    headers.agi = agi;
  }
  return headers;
}
