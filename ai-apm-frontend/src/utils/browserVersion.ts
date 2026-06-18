interface Browser {
  browser: string
  version: number
}

const getBrowserVersion = (browserType: string, UserAgent: string) => {
  let version  = '';
  switch (browserType) {
    case 'Chrome':
      version = UserAgent.match(/chrome\/([\d.]+)/)![1];
      break;
    case 'Edge':
      version = UserAgent.match(/edge\/([\d.]+)/)![1];
      break;
    case 'Firefox':
      version = UserAgent.match(/firefox\/([\d.]+)/)![1];
      break;
    case 'Opera':
      version = UserAgent.match(/opera\/([\d.]+)/)![1];
      break;
    case 'Safari':
      version = UserAgent.match(/version\/([\d.]+)/)![1];
      break;
  }
  return parseInt(version, 10);
}

const getBrowser = () => {
  const UserAgent = navigator.userAgent.toLowerCase();
  const browserInfo: any = {};
  const browserObj: any = {
    Chrome: UserAgent.indexOf('chrome') > -1 && UserAgent.indexOf('safari') > -1,
    Edge: UserAgent.indexOf('edge') > -1,
    Firefox: UserAgent.indexOf('firefox') > -1,
    Opera: UserAgent.indexOf('opera') > -1,
    Safari: UserAgent.indexOf('safari') > -1 && UserAgent.indexOf('chrome') === -1,
  };
  for (const key in browserObj) {
    if (browserObj.hasOwnProperty(key) && browserObj[key]) {
      browserInfo.browser = key;
      browserInfo.version = getBrowserVersion(key, UserAgent);
      break;
    }
  }
  return browserInfo;
}

export const isUpgradeBrowser = (browserList: Browser[]) => {
  const { browser = '', version = '' } = getBrowser()
  for (const objKey of browserList) {
    if (objKey.browser === browser && version < objKey.version) {
      return {
        browser,
        version,
        lowestVersion: objKey.version,
        isUpdate: true
      }
    }
  }
  return {
    browser,
    version,
    isUpdate: false
  }
}
