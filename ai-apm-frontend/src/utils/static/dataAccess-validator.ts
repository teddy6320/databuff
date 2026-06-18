
const textReg = new RegExp(/^\w+$/);
const nameValid = (value: string) => {
  if (!value) {
    return false;
  } else {
    return textReg.test(value)
  }
}
const emptyValid = (value: string) => {
  return !!String(value)
}

const endpointValid = (value: string) => {
  if (!value) {
    return false;
  } else {
    const ipPortRegex = new RegExp(/^(?:(?:25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)\.){3}(?:25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d):(3[0-1]\d{3}|32[0-6]\d{2}|327[0-5]\d|3276[0-7])$/);
    return ipPortRegex.test(value)
  }
}

const pathValid = (value: string) => {
  if (!value) {
    return false;
  } else {
    const pathReg = new RegExp(/^[a-zA-Z0-9/]+$/);
    return pathReg.test(value)
  }
}

const validConfigByType = (nodeData: any) => {
  const { name, type, originData } = nodeData || {};
  const config = originData?.config || {};
  let valid = true;
  // let errorMsg = '';
  if (!nameValid(name)) {
    valid = false;
    // errorMsg = '名称校验失败';
    return valid;
  }
  switch (type) {
    case 'webhookevent':
    case 'prometheusremotewrite':
      valid = endpointValid(config?.endpoint) && pathValid(config?.path)
      break;
    case 'skywalking':
    case 'otlp':
      if (config?.protocols?.grpc) {
        valid = endpointValid(config?.protocols?.grpc?.endpoint)
      } else if (config?.protocols?.http) {
        valid = (type === 'otlp' || pathValid(config?.protocols?.http?.path)) && endpointValid(config?.protocols?.http?.endpoint)
      } else {
        valid = false
      }
      break;
    // 处理器
    case 'insert':
    case 'update':
    case 'upsert':
      valid = Array.isArray(config?.actions) && config.actions.every((i: any) => emptyValid(i?.key) && emptyValid(i?.value));
      break;
    case 'delete':
      valid = Array.isArray(config?.actions) && config.actions.every((i: any) => emptyValid(i?.key));
      break;
    case 'extract':
      valid = Array.isArray(config?.actions) && config.actions.every((i: any) => emptyValid(i?.key) && emptyValid(i?.pattern));
      break;
    case 'hash':
      valid = Array.isArray(config?.actions) && config.actions.every((i: any) => emptyValid(i?.key));
      break;
    case 'convert':
      valid = Array.isArray(config?.actions) && config.actions.every((i: any) => emptyValid(i?.key) && emptyValid(i?.converted_type));
      break;
    case 'metricsgeneration':
      valid = Array.isArray(config?.rules) && config.rules.every((i: any) => {
        if (i?.type === 'calculate') {
          return emptyValid(i?.metric1) && emptyValid(i?.operation) && emptyValid(i?.metric2) && emptyValid(i?.name)
        } else if (i?.type === 'scale') {
          return emptyValid(i?.metric1) && emptyValid(i?.operation) && emptyValid(i?.scale_by) && emptyValid(i?.name)
        } else {
          return false
        }
      })
      break;
    case 'batch':
      valid = emptyValid(config?.send_batch_size) && emptyValid(String(config?.timeout).replace('ms', '')) && emptyValid(config?.send_batch_max_size)
      break;
    case 'filter':
      valid = emptyValid(config?.error_mode)
      break;
    case 'transform':
      const { metric_statements, trace_statements, log_statements } = config;
      valid = emptyValid(config?.error_mode) && (
        (Array.isArray(metric_statements) && !!metric_statements.length) ||
        (Array.isArray(trace_statements) && !!trace_statements.length) ||
        (Array.isArray(log_statements) && !!log_statements.length)
      );
      break;
  }

  return valid;
}


export default validConfigByType;
